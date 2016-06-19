int solenoid = 4;

void setup() {
  // initialize digital pin 13 as an output.
  pinMode(solenoid, OUTPUT);
}

// the loop function runs over and over again forever
void loop() {
  digitalWrite(solenoid, HIGH);   // turn the LED on (HIGH is the voltage level)
  delay(100);              // wait for a second
  digitalWrite(solenoid, LOW);    // turn the LED off by making the voltage LOW
  delay(1000);              // wait for a second
}
