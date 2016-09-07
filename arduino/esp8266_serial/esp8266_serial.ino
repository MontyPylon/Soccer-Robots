#include <SoftwareSerial.h>

SoftwareSerial esp8266(11, 10); // RX, TX

void setup() {
  // Open serial communications and wait for port to open:
  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }

  Serial.println("Goodnight moon!");
  //Serial.flush();
  
  // set the data rate for the SoftwareSerial port
  esp8266.begin(9600);
  while(!esp8266) {;}
  /**
  Serial.println("Printing to 9600 serial: AT");
  //Serial.flush();
  //mySerial.println("AT+RST");
  esp8266.write("AT+RST\r\n");
  if(esp8266.available()) {
    //Serial.println(esp8266.readString());
    String dataBack = esp8266.readString();
    Serial.print("Found this: ");
    Serial.println(dataBack);

    int okay = -1;
    okay = dataBack.indexOf("Ready");
    Serial.print("okay value: ");
    Serial.println(okay);

    if(okay >= 0) {
      Serial.println("Found okay");
    }
    /**
    if(esp8266.find("Ready")) {
      Serial.println("Found ready");
    }
    **
  }
  Serial.println("Done");
  //delay(3000);
  //esp8266.flush();
  //Serial.write(mySerial.read());
  /**
  if(esp8266.find("System")) {
    Serial.println("Found ready");
    //Serial.flush();
  }
  **/
}

void loop() { // run over and over
  if (esp8266.available()) {
    Serial.write(esp8266.read());
    //Serial.flush();
  }
  if (Serial.available()) {
    esp8266.write(Serial.read());
    //esp8266.flush();
  }
}
