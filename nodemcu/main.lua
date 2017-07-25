dofile("settings.lua")

wifi.setmode(wifi.STATION)
wifi.setphymode(wifi.PHYMODE_N)

wifi.sta.config(WIFI_SSID,WIFI_PASS)
wifi.sta.connect()

retries = 0

mytimer = tmr.create()
mytimer:register(1000, tmr.ALARM_AUTO, function()  verifConnect() end)
mytimer:start()

function verifConnect()

    connected = wifi.sta.getip() ~= nil 
    timedOut = retries > 10

    if(connected) then
        print("wifi connected")
        tmr.unregister(mytimer)       
        mqttConnectAndSend()
    end
    
    if (timedOut) then 
        tmr.unregister(mytimer)       
        print("wifi timed out")
    end
    retries = retries + 1
end

function mqttConnectAndSend()
 
    mqtt = mqtt.Client(node.chipid(), 120, MQTT_USER, MQTT_PASS)
    mqtt:on("connect", function(con) print ("mqtt connected") end)
    mqtt:on("offline", function(con) print ("mqtt offline") end) 
    
    mqtt:connect(MQTT_SERVER, MQTT_PORT, 0, function(conn) 
     temp, humi = readSensors()
     mqtt:publish("nodemcu/"..node.chipid().."/temperature",temp,0,0, function(conn) 
        print("sent temp "..temp) 
        mqtt:publish("nodemcu/"..node.chipid().."/humidity",humi,0,0, function(conn) 
            print("sent humi "..humi) 
            node.dsleep(OPT_SAMPLING)
        end)  
     end)
     
    end)
end

function readSensors()
    pin = 3
    status, temp, humi = dht.readxx(pin)
    return temp, humi
end

