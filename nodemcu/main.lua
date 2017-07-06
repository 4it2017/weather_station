WIFI_NAME = "netmatch-guest"
WIFI_PASS = "nmt2013gu3st"

wifi.setmode(wifi.STATION)
wifi.setphymode(wifi.PHYMODE_N)
wifi.sta.config(WIFI_NAME,WIFI_PASS)
wifi.sta.connect()

mytimer = tmr.create()
mytimer:register(1000, tmr.ALARM_AUTO, function()  verifConnect() end)
mytimer:start()

function verifConnect()
    connected=wifi.sta.getip()~=nil 
    print(connected)
    if (connected) then 
    tmr.unregister(mytimer)
    mqttConnect()
    end
end

function mqttConnect()
    print("connect to mqtt") 
    mqtt = mqtt.Client("nodemcu", 120, "idwlxdum", "ksflFfLEc1M6")
    mqtt:on("connect", function(con) print ("connected") end)
    mqtt:on("offline", function(con) print ("offline") end) 
    
    mqtt:connect("m20.cloudmqtt.com", 16691, 0, function(conn) 
     print("connected")
     mqtt:publish("nodemcu/1","my_message",0,0, function(conn) 
       print("sent") 
     end)
    end)
end


