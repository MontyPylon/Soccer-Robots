int rightB = 5; //5
int rightF = 3; //3
int leftF = 9; //9
int leftB = 6; //6
int keepBall = 7;
int releaseBall = 8;
int batteryLevel = 0;
int button1 = 5;
int button2 = 1;
int button3 = 4;

int power = 0;
bool startUp = true;
bool backwards = false;

void setup() {
  Serial.begin(9600);
  pinMode(leftF, OUTPUT);
  pinMode(leftB, OUTPUT);
  pinMode(rightF, OUTPUT);
  pinMode(rightB, OUTPUT);
  pinMode(keepBall, OUTPUT);
  pinMode(releaseBall, OUTPUT);
}

void loop() {
  if(startUp) {
    if(power == 150) {
      startUp = false;
    } else {
      power++;
    }
  } else {
    power--;
  }
  Serial.print("power: ");
  Serial.println(power);
  if(backwards) {
    analogWrite(rightB, power);
    analogWrite(leftB, power);
  } else {
    analogWrite(rightF, power);
    analogWrite(leftF, power);
  }
  digitalWrite(keepBall, HIGH);
  delay(20);

  if(power == 0) {
    //digitalWrite(keepBall, LOW);
    //digitalWrite(rightB, HIGH);
    //digitalWrite(leftB, HIGH);
    //delay(2000);
    digitalWrite(rightF, LOW);
    digitalWrite(leftF, LOW);
    digitalWrite(rightB, LOW);
    digitalWrite(leftB, LOW);
    power = 0;
    startUp = true;
    backwards = !backwards;
    delay(1000);
  }
  //delay(0);
  /**
  digitalWrite(keepBall, HIGH);
  digitalWrite(rightF, HIGH);
  digitalWrite(leftF, HIGH);
  delay(2000);
  digitalWrite(rightF, LOW);
  digitalWrite(leftF, LOW);
  delay(2000);
  
  digitalWrite(rightB, HIGH);
  digitalWrite(leftB, HIGH);
  delay(2000);
  digitalWrite(rightB, LOW);
  digitalWrite(leftB, LOW);
  delay(2000);
  digitalWrite(keepBall, LOW);
  delay(2000);

  digitalWrite(releaseBall, HIGH);  
  delay(2000);
  digitalWrite(releaseBall, LOW);
  delay(2000);
  **/
}
