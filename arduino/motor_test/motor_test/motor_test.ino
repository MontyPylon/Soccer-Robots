int rightB = 3; //5
int rightF = 5; //3
int leftF = 6; //9
int leftB = 9; //6
int keepBall = 7;
int releaseBall = 8;

void setup() {
  // put your setup code here, to run once:
  pinMode(leftF, OUTPUT);
  pinMode(leftB, OUTPUT);
  pinMode(rightF, OUTPUT);
  pinMode(rightB, OUTPUT);
  //pinMode(keepBall, OUTPUT);
  //pinMode(releaseBall, OUTPUT);
}

void loop() {

  digitalWrite(rightF, LOW);
  digitalWrite(leftF, LOW);
  digitalWrite(rightB, LOW);
  digitalWrite(leftB, LOW);
  
  digitalWrite(rightF, HIGH);
  delay(2000);
  digitalWrite(rightF, LOW);
  delay(2000);
  
  digitalWrite(leftF, HIGH);  
  delay(2000);
  digitalWrite(leftF, LOW);
  delay(2000);

  digitalWrite(rightB, HIGH);
  delay(2000);
  digitalWrite(rightB, LOW);
  delay(2000);
  
  digitalWrite(leftB, HIGH);  
  delay(2000);
  digitalWrite(leftB, LOW);
  delay(2000);
  /**
  digitalWrite(keepBall, HIGH);  
  delay(2000);
  digitalWrite(keepBall, LOW);
  delay(2000);

  digitalWrite(releaseBall, HIGH);  
  delay(2000);
  digitalWrite(releaseBall, LOW);
  delay(2000);
  **/
}
