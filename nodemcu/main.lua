print("wake")
dofile("settings.lua")

wifi.setmode(wifi.STATION)
wifi.setphymode(wifi.PHYMODE_N)

station_cfg={}
station_cfg.ssid=WIFI_SSID
station_cfg.pwd=WIFI_PASS
wifi.sta.config(station_cfg)
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
        rtctime.dsleep(OPT_SAMPLING)
    end
    retries = retries + 1
end

function mqttConnectAndSend()

    sntp.sync("ro.pool.ntp.org", function(sec, usec, server, info)
    
        time = rtctime.epoch2cal(rtctime.get())
        string_time = string.format("%04d/%02d/%02d %02d:%02d:%02d", time["year"], time["mon"], time["day"],time["hour"], time["min"], time["sec"])
        print(string.format("%04d/%02d/%02d %02d:%02d:%02d", time["year"], time["mon"], time["day"],time["hour"], time["min"], time["sec"]))
        
     
        mqtt = mqtt.Client(node.chipid(), 120, MQTT_USER, MQTT_PASS)
        mqtt:on("connect", function(con) print ("mqtt connected") end)
        mqtt:on("offline", function(con) print ("mqtt offline") end) 
        
        mqtt:connect(MQTT_SERVER, MQTT_PORT, 0, function(conn) 
        temp, humi, pressure = readSensors()
        mqtt:publish("nodemcu/"..node.chipid() , string_time.."|"..temp.."|"..humi.."|"..pressure, 1, 0, function(conn) 
            print("nodemcu/"..node.chipid().."|"..string_time.."|"..temp.."|"..humi.."|"..pressure) 
            node.dsleep(OPT_SAMPLING)
    --        mqtt:publish("nodemcu/"..node.chipid().."/humidity", humi, 1, 0, function(conn) 
    --            print("sent humi "..humi)   
    --            mqtt:publish("nodemcu/"..node.chipid().."/pressure", pressure, 1, 0, function(conn) 
    --                print("sent pressure "..pressure) 
    --                print("sleep")
    --            end)  
    --        end)  
        end) 
        end)
     end,
    function()
    print('failed!')
    end
    )
end

function readSensors()
    bme280.init(2,1)
    bmeT, bmeP, bmeH, bmeQNH = bme280.read(LOC_ALTITUDE)
    dhtStatus, dhtTemp, dhtHumi = dht.readxx(3)
        
    return dhtTemp, dhtHumi, bmeQNH/1000
end

