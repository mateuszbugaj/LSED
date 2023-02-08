# LSED
Live Stream External Device

Modify existing yaml config file at `src/main/resources/LSEDConfig.yaml` or create your own.
There, provide configurations for devices and streams as well as manage users, log files and other properties.

```yaml
deviceConfigDir: [xxx.yaml, yyy.yaml]
streamConfigDir: []
```

Example of a device config file:
```yaml
# Microscope
name: Microscope # Name showed in the app
portName: ttyUSB0 # USB port used for communication
portBaudRate: 9600
cameras: # List of cameras associated with the device
  - name: "Main View" # Camera name showed in the app
    portName: "/dev/video4" # Cameras can be shared with many devices
commands: # List of commands for this device
  - name: "Bed Light Switch" # Command name showed in the app
    description: "Switch bed light ON and OFF." # Showed to users with !help
    prefix: "bedLight" # prefix for using the command
    params: # List of command parameters
      - name: "State" # Commands can share name as long as number or type of parameters is unique
        type: String # Type can be String or Integer
        values: [ "on", "off" ] # Optional list of allowed values
    devicePrefix: "lgt bed" # prefix sent to the device
  - name: "Top Light Switch"
    description: "Switch top light ON and OFF."
    prefix: "topLight"
    params:
      - name: "State"
        type: String
        values: [ "on", "off" ]
    devicePrefix: "lgt top"
```
Users can send command to the device typing `!Microscope bedLight on`.

Example of Twitch stream config file:
```yaml
service: twitch
name: UserName
token: oauth:xxx
```

Example of YouTube stream config file:
```yaml
service: youtube
name: UserName
channelId: <id>
token: <token>
```

Run by passing the config file path with:
```bash
mvn javafx:run -Djavafx.args="src/main/resources/LSEDConfig.yaml"
```