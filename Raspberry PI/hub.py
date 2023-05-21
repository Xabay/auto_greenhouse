import serial
import socket
import threading
import time
import select
import queue
import traceback

#Global variables
cur_vals = [0, 0, 0, 0]
cur_lock = threading.Lock()
file_lock = threading.Lock()
ser_queue = queue.Queue()


#Function communicating with the arduino
def arduino_serv():
    #Thread variables
    LOG_INTERVAL = 300
    LOG_TIMEOUT = 86400

    #Establish connection with arduino
    ser = serial.Serial('/dev/ttyACM0', 9600)
    ser.reset_input_buffer()
    ser.reset_output_buffer()
    last_log = 0

    
    #Sync message timings
    while(ser.readline().decode().rstrip() != "-"):
        pass

    while(True):  
        try:
            #Read and process if there is incoming data
            if (ser.in_waiting > 0):
                cur_ind = 0
                received = ser.readline().decode().rstrip()
                while (received != "-"):
                    cur_vals[cur_ind] = int(received)
                    cur_ind += 1
                    while (ser.in_waiting == 0):
                        pass
                    received = ser.readline().decode().rstrip()
                
                #Log if sufficient time has passed
                cur_logs = []
                cur_time = int(time.time())
                if ( cur_time - last_log >= LOG_INTERVAL):
                    with open('/home/pi/Documents/uveghaz_m/data/measurements', 'r') as l:
                        cur_logs = l.readlines()
                    file_lock.acquire()
                    with open('/home/pi/Documents/uveghaz_m/data/measurements', 'w') as l:
                        for log in cur_logs:
                            if ( cur_time - int(log.split(';')[0]) < LOG_TIMEOUT ):
                                l.write(log)
                        l.write('{};{}\n'.format(cur_time, ';'.join(str(val) for val in cur_vals)))
                    file_lock.release()
                    last_log = cur_time

            #Get and send commands given by other thread
            while (not ser_queue.empty()):
                to_send = ser_queue.get()
                ser.write(to_send.encode())
                if(to_send[0] == "3"):
                    time.sleep(0.5)
                ser.flush()

        #Do not stop upon error
        except Exception:
            with open('/home/pi/Documents/uveghaz_m/logs/ard_errors', 'a') as f:
                traceback.print_exc(file=f)
            while(ser.readline().decode().rstrip() != "-"):
                pass




#Function communicating with the application
def app_serv():

    #Thread variables
    manual_control = False
    attribs = ['m', 'h', 't', 'w']
    pin_map = {
        'm' : 2,
        'h' : 3,
        't' : 4,
        'l' : 5,
        'f' : 6,
    }
    pin_vals = [{
        'm' : 0,
        'h' : 0,
        't' : 0,
        'l' : 0,
        'f' : 0,
    }, 
    {
        'm' : 0,
        'h' : 0,
        't' : 0,
        'l' : 0,
        'f' : 0,
    },
    {
        'm' : 0,
        'h' : 0,
        't' : 0,
        'l' : 0,
        'f' : 0,
    }]
    goal_vals = {
        'm' : 0,
        'h' : 0, 
        't' : 0,
        'ls': 0,
        'le': 0,
        'fs': 0,
        'fe': 0
    }
    
    #Function for setting pin values
    def pin_set(attr, is_on):
        if pin_vals[2][attr] != is_on:

            #Specific commands for humidifier
            if attr == "h":
                if is_on:
                    ser_queue.put("{};1;".format(pin_map["h"]))
                    ser_queue.put("{};0;".format(pin_map["h"]))
                else:
                    ser_queue.put("{};1;".format(pin_map["h"]))
                    ser_queue.put("{};0;".format(pin_map["h"]))
                    ser_queue.put("{};1;".format(pin_map["h"]))
                    ser_queue.put("{};0;".format(pin_map["h"]))
            
            #General commands for other modules
            else:
                ser_queue.put("{};{};".format(pin_map[attr], is_on))
            pin_vals[2][attr] = is_on




    #Establish connection with application
    PI_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    PI_sock.bind(('0.0.0.0', 12345))
    PI_sock.listen()
    clients = [PI_sock]


    while(True):
        try:
            readable, _, _ = select.select(clients, clients, clients)
            for s in readable:

                #Add new client
                if s is PI_sock:
                    new_cli, cli_addr = s.accept()
                    clients.append(new_cli)

                #Process received message
                else:
                    data = s.recv(1024)
                    if data:
                        message_raw = data.decode('UTF-8')
                        message = message_raw.split(';')
                        
                        #Send plot dataS
                        if (message[0] == 'p'):
                            ind = attribs.index(message[1]) + 1
                            times = ''
                            vals = ''
                            file_lock.acquire()
                            with open('/home/pi/Documents/uveghaz_m/data/measurements', 'r') as f:
                                for line in f.readlines():
                                    line_vals = line.split(';')
                                    times += "," + line_vals[0]
                                    vals += "," + line_vals[ind]
                            file_lock.release()
                            times = times[1:]
                            vals = vals[1:]
                            graph = times + ";" + vals
                            s.send(graph.encode('UTF-8'))

                        #Send current values
                        if (message[0] == 'v'):
                            if message[1] in attribs:
                                s.send(str(cur_vals[attribs.index(message[1])]).encode('UTF-8'))
                            else:
                                s.send('err'.encode('UTF-8'))

                        #Send current goal values
                        if (message[0] == 'g'):
                            if message[1] in goal_vals.keys():
                                s.send(str(goal_vals[message[1]]).encode('UTF-8'))
                            else:
                                s.send('err'.encode('UTF-8'))

                        #Set goal values
                        if (message[0] == 's'):
                            if message[1] in goal_vals.keys():
                                goal_vals[message[1]] = int(message[2])
                                s.send('ack'.encode('UTF-8'))
                            else:
                                s.send('err'.encode('UTF-8'))

                        #Switch control mode
                        if (message[0] == 'c'):
                            pin_vals[manual_control] = pin_vals[2].copy()
                            manual_control = bool(int(message[1]))
                            for key in pin_vals[int(manual_control)].keys():
                                pin_set(key, pin_vals[int(manual_control)][key])
                            s.send("ack".encode('UTF-8'))

                        #Query manual status of components
                        if (message[0] == 'q'):
                            if message[1] in pin_vals[manual_control].keys():
                                s.send(str(int(pin_vals[2][message[1]])).encode('UTF-8'))
                            elif message[1] == 'c':
                                s.send(str(int(manual_control)).encode('UTF-8'))
                            else:
                                s.send("err".encode('UTF-8'))

                        #Toggle components manually
                        if (message[0] == 't'):
                            if message[1] in pin_vals[manual_control].keys():
                                s.send("ack".encode('UTF-8'))
                                pin_set(message[1], int(message[2]))
                            else:
                                s.send("err".encode('UTF-8'))
                    else:
                        s.close()
                        clients.remove(s)

            #Automatic control logic
            if (not manual_control):
                #Soil moisture
                if cur_vals[0] < goal_vals["m"]:
                    pin_set("m", 1)
                else:
                    pin_set("m", 0)

                #Humidity
                if cur_vals[1] < goal_vals["h"]:
                    pin_set("h", 1)
                else:
                    pin_set("h", 0)

                #Temperature
                if cur_vals[2] < goal_vals["t"]:
                    pin_set("t", 1)
                else:
                    pin_set("t", 0)

                #Get current time
                loc_time = time.localtime(time.time())
                cur_time = int(time.strftime('%H', loc_time)) * 3600 + int(time.strftime('%M', loc_time)) * 60 + int(time.strftime('%S', loc_time))
                
                #Lights
                if goal_vals["ls"] > goal_vals["le"]:
                    if cur_time >= goal_vals["ls"]  or cur_time < goal_vals["le"]:
                        pin_set("l", 1)
                    else:
                        pin_set("l", 0)
                else:
                    if cur_time >= goal_vals["ls"]  and cur_time < goal_vals["le"]:
                        pin_set("l", 1)
                    else:
                        pin_set("l", 0)

                #Fans
                if goal_vals["fs"] > goal_vals["fe"]:
                    if cur_time >= goal_vals["fs"]  or cur_time < goal_vals["fe"]:
                        pin_set("f", 1)
                    else:
                        pin_set("f", 0)
                else:
                    if cur_time >= goal_vals["fs"]  and cur_time < goal_vals["fe"]:
                        pin_set("f", 1)
                    else:
                        pin_set("f", 0)
        except Exception:
            with open('/home/pi/Documents/uveghaz_m/logs/app_errors', 'a') as f:
                traceback.print_exc(file=f)           
    


#Main, start all the threads
if __name__ == '__main__':
    t_ard = threading.Thread(target = arduino_serv)
    t_app = threading.Thread(target = app_serv)
    t_ard.start()
    t_app.start()