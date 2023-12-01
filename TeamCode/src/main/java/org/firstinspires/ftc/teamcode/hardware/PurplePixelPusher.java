package org.firstinspires.ftc.teamcode.hardware;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

@Config
public class PurplePixelPusher {
    Servo ppp;
    public static double pppClampPos = 0.065;
    public static double pppOpenPos = 0.15;

    boolean state;

    public PurplePixelPusher(HardwareMap hwmap){
        ppp = hwmap.get(Servo.class, "ppp");
        setState(true);
    }

    public void setState(boolean state){
        if (state) ppp.setPosition(pppClampPos);
        else ppp.setPosition(pppOpenPos);
        this.state = state;
    }
    public boolean getState(){return state;}

    public void displayDebug(Telemetry telemetry){
        telemetry.addLine("TRIPLE P");
        telemetry.addData("State", getState());
        telemetry.addData("Position", ppp.getPosition());
    }
}