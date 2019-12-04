package frc.robot.subsystems.drive;

import edu.wpi.first.wpilibj.command.Subsystem;
import com.team7419.Initers;
import com.team7419.MotorGroup;
import com.team7419.TalonFuncs;

import frc.robot.RobotMap;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

public class DriveBaseSub extends Subsystem {

	private VictorSPX leftFol;
	private VictorSPX rightFol;
	public TalonSRX leftMast;
  public TalonSRX rightMast;
  public MotorGroup leftSide; 
  public MotorGroup rightSide;
  
  public DriveBaseSub(){
    leftFol = new VictorSPX(RobotMap.leftVictor.value);
		rightFol = new VictorSPX(RobotMap.rightVictor.value);
		leftMast = new TalonSRX(RobotMap.leftTalon.value);
		rightMast = new TalonSRX(RobotMap.rightTalon.value);

    leftSide = new MotorGroup(leftMast, leftFol);
    rightSide = new MotorGroup(rightMast, rightFol);

    Initers.initVictors(leftFol, rightFol);

    leftSide.followMaster();
    rightSide.followMaster();

    TalonFuncs.configEncoder(leftMast);
    TalonFuncs.configEncoder(rightMast);
  }

  @Override
  public void initDefaultCommand() {} 
    //imma not do this bc we want to feed arcade constructor useful things in Robot.java
    
  public MotorGroup getLeftSide(){return leftSide;}
  public MotorGroup getRightSide(){return rightSide;}
  
}