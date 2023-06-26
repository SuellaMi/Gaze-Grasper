#include <Wire.h>
#include <DynamixelShield.h>
#if defined(ARDUINO_AVR_UNO)
#include <SoftwareSerial.h>
SoftwareSerial soft_serial(7, 8); // DYNAMIXELShield UART RX/TX
#define DEBUG_SERIAL soft_serial
#endif
int relayPin = 9;
const uint8_t DXL_ID = 1;
const float DXL_PROTOCOL_VERSION = 2.0;

DynamixelShield dxl;

//This namespace is required to use Control table item names
using namespace ControlTableItem;

void setup() {
  Wire.begin(9); // Join i2c bus with address #4
  Wire.onReceive(receiveEvent); // register event
  // put your setup code here, to run once:
  pinMode(relayPin, OUTPUT); 
  // For Uno, Nano, Mini, and Mega, use UART port of DYNAMIXEL Shield to debug.
  DEBUG_SERIAL.begin(115200);
  // Set Port baudrate to 57600bps. This has to match with DYNAMIXEL baudrate.
  dxl.begin(57600);
  // Set Port Protocol Version. This has to match with DYNAMIXEL protocol version.
  dxl.setPortProtocolVersion(DXL_PROTOCOL_VERSION);
  // Get DYNAMIXEL information
  dxl.ping(DXL_ID);
  // Turn off torque when configuring items in EEPROM area
  dxl.torqueOff(DXL_ID);
  dxl.setOperatingMode(DXL_ID, OP_POSITION);
  dxl.torqueOn(DXL_ID);
}

void loop() {

}
void receiveEvent(int bytes) {
  while (Wire.available()) {
    int c = Wire.read(); // receive byte as a character
if(c == 1)
{
digitalWrite(relayPin,HIGH);

}
if(c == 2)
{
digitalWrite(relayPin,LOW);
dxl.setGoalPosition(DXL_ID, 120, UNIT_DEGREE);
  delay(1000);
}
if(c == 3)
{
digitalWrite(relayPin,HIGH);
}
if(c == 4)
{
digitalWrite(relayPin,LOW);
}

  }
}
