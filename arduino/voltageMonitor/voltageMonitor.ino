int analogPin = 3;     // potentiometer wiper (middle terminal) connected to analog pin 3
double val = 0;           // variable to store the value read
double voltage = 0;

void setup()
{
  Serial.begin(9600);          //  setup serial
}

void loop()
{
  val = analogRead(analogPin);    // read the input pin
  voltage = (val / 1024) * 5;
  Serial.println(val);             // debug value
  Serial.println(voltage);
  Serial.println("----------");
  delay(1000);
}
