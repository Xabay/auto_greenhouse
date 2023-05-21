import matplotlib
from statistics import mean
from statistics import median
from matplotlib import pyplot as plt


air_m = []
wat_m = []

air_w = []
wat_w = []

with open("air_1h.txt", "r") as f:
    lines = f.readlines()
    for l in lines:
        l = l.rstrip()
        air_m.append(int(l.split(";")[1]))
        air_w.append(int(l.split(";")[4]))

with open("water_1h.txt", "r") as f:
    lines = f.readlines()
    for l in lines:
        l = l.rstrip()
        wat_m.append(int(l.split(";")[1]))
        wat_w.append(int(l.split(";")[4]))

print("Air moisture: ", median(air_m))
print("Water moisture: ", median(wat_m))
print("Air level: ", median(air_w))
print("Water level: ", median(wat_w))


matplotlib.rcParams.update({'font.size': 22})

fig, (ax1, ax2) = plt.subplots(1, 2, sharey = True)
fig.suptitle("Vízszint")
ax1.plot(air_w)
ax1.set_xlabel("Mérések száma")
ax1.set_ylabel("Mért érték")
ax1.set_title("Levegőn")
ax2.plot(wat_w)
ax2.set_xlabel("Mérések száma")
ax2.set_title("Vízben")
plt.show()
