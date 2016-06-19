#include <SoftwareSerial.h>
#define SSID "Cook1"
#define PASS "monalisa2"
SoftwareSerial esp8266(10, 11); // RX, TX

#include <Adafruit_NeoPixel.h>
#define LEDSTRIP 2
Adafruit_NeoPixel strip = Adafruit_NeoPixel(5, LEDSTRIP, NEO_GRB + NEO_KHZ800);

uint32_t red = strip.Color(255, 0, 0);
uint32_t green = strip.Color(0, 255, 0);
uint32_t blue = strip.Color(0, 0, 255);
uint32_t none = strip.Color(0, 0, 0);

int rightB = 6;
int rightF = 9;
int leftF = 5;
int leftB = 3;
int shoot = 12;

int voltageMonitor = 3;
double deadBattery = 7.3;
double fullBattery = 8.4;
double analogVal = 0;
double battVoltage = 0;

boolean first = true;
unsigned long time;
unsigned long oldTime = 0;

char previous;

char eight;
char seven;
char six;
char five;
char four;
char three;
char two;
char one;

String command = "test";
boolean shot = false;
unsigned long shotTime = 0;
boolean shotReset = false;

int power = 100;
int turnPower = 100;

void setup()
{
  pinMode(leftF, OUTPUT);
  pinMode(leftB, OUTPUT);
  pinMode(rightF, OUTPUT);
  pinMode(rightB, OUTPUT);
  pinMode(shoot, OUTPUT);
  strip.begin();
  strip.show(); // Initialize all pixels to 'off'
  //Serial USB connection from Arduino to USB
  Serial.begin(9600);
  //while (!Serial); //wait for Serial to connect
  Serial.println("USB to Arduino has been established");

  //Serial from Arduino to ESP8266
  esp8266.begin(9600); 
  setColor("red");
  
  while (!esp8266); //wait for Serial to connect
  Serial.println("Arduino to ESP8266 has been established");
  setColor("blue");
  delay(200);

  setColor("red");
  esp8266.println("AT+RST");
  Serial.println("Resetting ESP8266 module");
  esp8266.flush();
  
  delay(2000);
  if (esp8266.find("Ready") || esp8266.find("ready"))
  {
    Serial.println("ESP8266 module is ready");
    setColor("blue");
    delay(200);
  }
  else
  {
    Serial.println("ESP8266 module has no response...");
    while (1);
  }

  setColor("red");
  delay(1000);

  //connect to the wifi
  boolean connected = false;

  //Attempt to connect to wifi 5 times
  for (int i = 0; i < 5; i++)
  {
    if (connectWiFi())
    {
      connected = true;
      break;
    }
  }

  if (!connected) {
    Serial.println("Could not connect to wifi after 5 attemps");
    while (1);
  }

  //Set connection mode to multiple
  Serial.println("Set connection mode to multiple");
  esp8266.println("AT+CIPMUX=1");
  delay(1000);
  if (esp8266.find("OK"))
  {
    Serial.println("OK");
  }
  else
  {
    Serial.println("ESP8266 AT+CIPMUX=1 command did not work...");
    while (1);
  }

  Serial.println("Starting the web server");
  esp8266.println("AT+CIPSERVER=1,80");
  delay(1000);
  if (esp8266.find("OK"))
  {
    Serial.println("OK");
  }
  else
  {
    Serial.println("ESP8266 AT+CIPSERVER=1,80 command did not work...");
    while (1);
  }

  //esp8266.println("AT+CIFSR");
  //Serial.println(esp8266.read());
  Serial.println("-----------------------------------");
  Serial.println("|        ESP8266 is ready!        |");
  Serial.println("-----------------------------------");
  setColor("green");
}

void loop()
{
  /**
  if (esp8266.available()) {
    Serial.write(esp8266.read());
  }
  if (Serial.available()) {
    esp8266.write(Serial.read());
  }
  **/
  
  if(esp8266.available()) {
    checkDirButtons();
  }
  
}

void checkDirButtons() {
  char c = esp8266.read();
  //esp8266.flush();
  //Serial.print("Char: ");
  //Serial.println(c);
  
  if(c == 'x') {
    /**
    Serial.println("--------------");
    Serial.println("Grabbed DATA");
    Serial.print("Previous 8: ");
    Serial.print(one);
    Serial.print(two);
    Serial.print(three);
    Serial.print(four);
    Serial.print(five);
    Serial.print(six);
    Serial.print(seven);
    Serial.println(eight);
    **/
    
    String leftString = "aaaa";
    leftString.setCharAt(0, one);
    leftString.setCharAt(1, two);
    leftString.setCharAt(2, three);
    leftString.setCharAt(3, four);
    //Serial.print("Left String: ");
    //Serial.println(leftString);
    int leftPower = leftString.toInt();

    String rightString = "aaaa";
    rightString.setCharAt(0, five);
    rightString.setCharAt(1, six);
    rightString.setCharAt(2, seven);
    rightString.setCharAt(3, eight);
    //Serial.print("Right String: ");
    //Serial.println(rightString);
    int rightPower = rightString.toInt();

    Serial.print("Left power: ");
    Serial.println(leftPower);
    Serial.print("Right power: ");
    Serial.println(rightPower);

    if(leftPower > 0 && leftPower <=255) {
      digitalWrite(leftB, LOW);
      analogWrite(leftF, leftPower);
    } else if(leftPower == 0) {
      digitalWrite(leftF, LOW);
      digitalWrite(leftB, LOW);
    } else if(leftPower < 0 && leftPower >= -255) {
      digitalWrite(leftF, LOW);
      int posLeftPower = abs(leftPower);
      analogWrite(leftB, posLeftPower);
    }

    if(rightPower > 0 && rightPower <=255) {
      digitalWrite(rightB, LOW);
      analogWrite(rightF, rightPower);
    } else if(rightPower == 0) {
      digitalWrite(rightF, LOW);
      digitalWrite(rightB, LOW);
    } else if(rightPower < 0 && rightPower >= -255) {
      digitalWrite(rightF, LOW);
      int posRightPower = abs(rightPower);
      analogWrite(rightB, posRightPower);
    }
  }

  previous = c;

  //grab the last 8 chars
  one = two;
  two = three;
  three = four;
  four = five;
  five = six;
  six = seven;
  seven = eight;
  eight = c;
  
  /**
  if(command.length() < 10) {
    command += c;
  } else {
    command.remove(0,1);
    command += c;
    Serial.println(command);
  }
  **/
  
}

void sendBatteryLevel() {
  analogVal = analogRead(voltageMonitor);
  battVoltage = ((analogVal / 1024) * 5 * 2);
  double bottom = fullBattery - deadBattery;
  double top = battVoltage - deadBattery;
  double final = (top / bottom) * 100;
  int finalInt = final;

  if(finalInt > 0 && finalInt < 100) {
    String data = (String) "z " + finalInt + "\n";
    esp8266.print("AT+CIPSEND=0,");
    esp8266.println(data.length());
    if(esp8266.find( ">" ))
    {
      esp8266.println(data);
      Serial.println(finalInt);
      Serial.println("----------");
    }
  }
}

boolean connectWiFi()
{
  esp8266.println("AT+CWMODE=1");
  String cmd = "AT+CWJAP=\"";
  cmd += SSID;
  cmd += "\",\"";
  cmd += PASS;
  cmd += "\"";
  esp8266.println(cmd);
  Serial.println(cmd);
  
  delay(10000);
  
  if (esp8266.find("OK"))
  {
    Serial.println("OK, Connected to WiFi.");
    return true;
  } else
  {
    Serial.println("Can not connect to the WiFi.");
    return false;
  }
}

void setColor(String color) {

  if(color == "red") {
    for(int a = 0; a < 5; a++) {
      strip.setPixelColor(a, red);
    }
    strip.show();
  } else if(color == "green") {
    for(int a = 0; a < 5; a++) {
      strip.setPixelColor(a, green);
    }
    strip.show();
  } else if(color == "blue") {
    for(int a = 0; a < 5; a++) {
      strip.setPixelColor(a, blue);
    }
    strip.show();
  } else if(color == "none") {
    for(int a = 0; a < 5; a++) {
      strip.setPixelColor(a, none);
    }
    strip.show();
  }
  
}

