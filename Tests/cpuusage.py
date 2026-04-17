import psutil
import time
# Get the CPU usage as a percentage
cpu_usage_percent = psutil.cpu_percent()

print(f"CPU Usage: {cpu_usage_percent}%")

def getCpuUsage():
    cpu_usage_percent = psutil.cpu_percent()

    print(f"CPU Usage: {cpu_usage_percent}%")
    time.sleep(2)
    getCpuUsage()

if __name__ == "__main__":
    getCpuUsage()

def waitForNormalCpuUsage(count=None,step=0):
    if step > 120 :
        return False #unable to have a normal cpu usage, redeployment
    cpu_usage_percent = psutil.cpu_percent()
    if cpu_usage_percent > 15:
        time.sleep(1)
        return waitForNormalCpuUsage(0,step+1)
    else:
        cp=1
        if count:
            cp=count+1
        if cp >= 2:
            #the cpu need to be free during three seconds interval in other consider it free
            return True
        else:
            time.sleep(2)
            return waitForNormalCpuUsage(cp,step+1)
    