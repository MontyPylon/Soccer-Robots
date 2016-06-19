#include <SoftwareSerial.h>
#define SSID "MOTOROLA-34A73"
#define PASS "963debc469728bd74ea5"
SoftwareSerial esp8266(11, 10); // RX, TX

int rightB = 6;
int rightF = 9;
int leftF = 5;
int leftB = 3;

boolean link = false;
unsigned long time = 0;

char eight;
char seven;
char six;
char five;
char four;
char three;
char two;
char one;

int dbg = 0;

void setup()
{
  pinMode(leftF, OUTPUT);
  pinMode(leftB, OUTPUT);
  pinMode(rightF, OUTPUT);
  pinMode(rightB, OUTPUT);
  
  //Serial USB connection from Arduino to USB
  Serial.begin(9600);
  debug("USB to Arduino has been established", 1);

  //Serial from Arduino to ESP8266
  esp8266.begin(9600); 
  
  while (!esp8266); //wait for Serial to connect
  Serial.println("Arduino to ESP8266 has been established");

  
  debug("Resetting ESP8266 module", 1);
  if(sendData("AT+RST", 2000, "Ready")) {
    debug("ESP8266 module is ready", 1);
  } else {
    Serial.println("ESP8266 module is ready");
    debug("ESP8266 module has no response...", 1);
  }

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
  esp8266.println("AT+CIPSERVER=1,9090");
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
  
  Serial.println("-----------------------------------");
  Serial.println("|        ESP8266 is ready!        |");
  Serial.println("-----------------------------------");
}

void loop()
{ 
  if(esp8266.available()) {
    checkDirButtons();
  }

  if(link) {
    time = millis();
  }
}

void checkDirButtons() {
  char c = esp8266.read();
  
  if(c == 'x') {
    
    //Serial.print("Time: ");
    //Serial.println(time);
    
    String leftString = "aaaa";
    leftString.setCharAt(0, one);
    leftString.setCharAt(1, two);
    leftString.setCharAt(2, three);
    leftString.setCharAt(3, four);
    int leftPower = leftString.toInt();

    String rightString = "aaaa";
    rightString.setCharAt(0, five);
    rightString.setCharAt(1, six);
    rightString.setCharAt(2, seven);
    rightString.setCharAt(3, eight);
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

  if(six == 'L' && seven == 'i' && eight == 'n' && c == 'k') {
    Serial.println("LINKED UP FAM!");
    time = millis();
    Serial.print("START TIME IS: ");
    Serial.println(time);
    link = true;
  } else if(six == 'l' && seven == 'i' && eight == 'n' && c == 'k') {
    Serial.println("UNLINKED boi");
    link = false;
    time = 0;
  }

  //grab the last 8 chars
  one = two;
  two = three;
  three = four;
  four = five;
  five = six;
  six = seven;
  seven = eight;
  eight = c;
}

void sendData(String data, int delayTime) {
  
  esp8266.println(data);
  delay(delayTime);
}

boolean sendData(String data, int delayTime, String lookFor) {
  esp8266.println(data);
  delay(delayTime);
  if (esp8266.find("Ready"))
  {
    return true;
  } else {
    return false;
  }
}

void debug(String message, int importance) {
  if(importance > dbg) {
    Serial.println(message);
  }
}

boolean connectWiFi()
{
  esp8266.println("AT+CWMODE=1");
  delay(1000);
  String cmd = "AT+CWJAP=\"";
  cmd += SSID;
  cmd += "\",\"";
  cmd += PASS;
  cmd += "\"";
  esp8266.println(cmd);
  Serial.println(cmd);
  
  //delay(12000);
  
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

