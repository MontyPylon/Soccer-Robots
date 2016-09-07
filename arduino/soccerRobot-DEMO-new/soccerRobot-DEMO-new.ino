#include <SoftwareSerial.h>
#include <Adafruit_NeoPixel.h>
SoftwareSerial esp8266(11, 10); // RX, TX
#define LED 12
Adafruit_NeoPixel strip = Adafruit_NeoPixel(5, LED, NEO_GRB + NEO_KHZ800);

uint32_t red = strip.Color(255, 0, 0);
uint32_t green = strip.Color(0, 255, 0);
uint32_t blue = strip.Color(0, 0, 255);
uint32_t none = strip.Color(0, 0, 0);

int rightB = 3;
int rightF = 5;
int leftF = 6;
int leftB = 9;
int keepBall = 7;
int releaseBall = 8;

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

void setup()
{
  pinMode(leftF, OUTPUT);
  pinMode(leftB, OUTPUT);
  pinMode(rightF, OUTPUT);
  pinMode(rightB, OUTPUT);
  pinMode(keepBall, OUTPUT);
  pinMode(releaseBall, OUTPUT);
  
  strip.begin();
  strip.show(); // Initialize all pixels to 'off'
  strip.setBrightness(25);
  
  setColor("blue"); // Set all pixels to blue to see if a message fails or succeeds with red or green
  //Serial from Arduino to ESP8266
  esp8266.begin(9600);
  
  while (!esp8266); //wait for Serial to connect

  delay(2000);

  // Send reset command
  esp8266.write("AT+RST\r\n");
  if(esp8266.available()) {
    delay(1000); // Give time for the module to reset
    String dataBack = esp8266.readString();

    int okay = -1;
    okay = dataBack.indexOf("OK");
    //Serial.print("okay value: ");
    //Serial.println(okay);

    if(okay >= 0) {
      strip.setPixelColor(0, green);
      strip.show();
    } else {
      debug("ESP8266 module is not ready", 1);
      strip.setPixelColor(0, red);
      strip.show();
      while (1);
    }
  }

  // Set wifi mode to soft access point
  esp8266.write("AT+CWMODE=3\r\n");
  if(esp8266.available()) {
    String dataBack = esp8266.readString();
    debug("Found this: ", 2);
    debug(dataBack, 2);

    int okay = -1;
    okay = dataBack.indexOf("OK");
    //Serial.print("okay value: ");
    //Serial.println(okay);
    int nochange = -1;
    nochange = dataBack.indexOf("no change");
    //Serial.print("no change value: ");
    //Serial.println(nochange);
    

    if(okay >= 0 || nochange >= 0) {
      debug("AT+CWMODE=2 set to 2", 1);
      strip.setPixelColor(1, green);
      strip.show();
    } else {
      debug("ESP8266 AT=CWMODE=2 command did not work...", 1);
      strip.setPixelColor(1, red);
      strip.show();
      while (1);
    }
  }

  // Set connection mode to multiple
  esp8266.write("AT+CIPMUX=1\r\n");
  if(esp8266.available()) {
    String dataBack = esp8266.readString();
    debug("Found this: ", 2);
    debug(dataBack, 2);

    int okay = -1;
    okay = dataBack.indexOf("OK");
    //Serial.print("okay value: ");
    //Serial.println(okay);

    if(okay >= 0) {
      debug("AT+CIPMUX=1 set to 1", 1);
      strip.setPixelColor(2, green);
      strip.show();
    } else {
      debug("ESP8266 AT+CIPMUX=1 command did not work...", 1);
      strip.setPixelColor(2, red);
      strip.show();
      while (1);
    }
  }

  // Starting the web server
  esp8266.write("AT+CIPSERVER=1,4242\r\n");
  if(esp8266.available()) {
    String dataBack = esp8266.readString();
    debug("Found this: ", 2);
    debug(dataBack, 2);

    int okay = -1;
    okay = dataBack.indexOf("OK");
    //Serial.print("okay value: ");
    //Serial.println(okay);

    if(okay >= 0) {
      debug("Server started on port 4242", 1);
      strip.setPixelColor(3, green);
      strip.show();
    } else {
      debug("ESP8266 AT+CIPSERVER=1,4242 command did not work...", 1);
      strip.setPixelColor(3, red);
      strip.show();
      while (1);
    }
  }
  
  debug("-----------------------------------", 1);
  debug("|        ESP8266 is ready!        |", 1);
  debug("-----------------------------------", 1);
  strip.setPixelColor(4, green);
  strip.show();
  delay(3000);
  setColor("none");
  strip.show();
}

void loop()
{ 
  if(esp8266.available()) {
    checkDirButtons();
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
    strip.setPixelColor(1, green);
    strip.show();
    link = true;
  } else if(six == 'l' && seven == 'i' && eight == 'n' && c == 'k') {
    Serial.println("UNLINKED boi");
    strip.setPixelColor(1, red);
    strip.show();
    link = false;
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

void debug(String message, int importance) {
  //if(importance <= dbg) {
    //Serial.println(message);
    //Serial.flush();
  //}
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

