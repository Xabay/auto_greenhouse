#include <string.h>
#include "DHT.h"

//declaring constants and global variables
const int AIR_M = 714;
const int WAT_M = 341;
const int AIR_W = 65;
const int WAT_W = 480;
const int REPS = 10;


int m_sum, t_sum, h_sum, w_sum, m_val, w_val;


DHT dht(8, DHT11);

void setup() {
  Serial.begin(9600);
  pinMode(2, OUTPUT);  //water pump
  pinMode(3, OUTPUT);  //humidifier
  pinMode(4, OUTPUT);  //heating element
  pinMode(5, OUTPUT);  //lights
  pinMode(6, OUTPUT);  //fans
  pinMode(7, OUTPUT);  //water level sensor power
  pinMode(A0, INPUT);  //soil sensor
  pinMode(A1, INPUT);  //water level sensor

  digitalWrite(7, 0);

  dht.begin();         //start temp and humidity sensor
}

void loop() {

  //Initialize values
  m_sum = 0;
  w_sum = 0;
  t_sum = 0;
  h_sum = 0;

  //Make the measurements, normalize
  digitalWrite(7, 1);
  for(int i=0; i<REPS; ++i) {
    delay(10);
    m_val = analogRead(A0);
    w_val = analogRead(A1);
    t_sum += dht.readTemperature();
    h_sum += dht.readHumidity();
    
    m_sum += map(m_val, WAT_M, AIR_M, 100, 0); 
    w_sum += map(w_val, WAT_W, AIR_W, 100, 0);
  }
  digitalWrite(7, 0);

  //Send results
  Serial.println(m_sum/REPS);
  Serial.println(h_sum/REPS);
  Serial.println(t_sum/REPS);
  Serial.println(w_sum/REPS);
  Serial.println("-");
  Serial.flush();
}

//Called, if there is incoming data
//Receive commands, set pins accordingly
void serialEvent() {
  while(Serial.available()) {
    int pin = Serial.parseInt();
    Serial.read();
    while(Serial.available() == 0) {}
    int state = Serial.parseInt();
    Serial.read();
    digitalWrite(pin, state);
  }
}



