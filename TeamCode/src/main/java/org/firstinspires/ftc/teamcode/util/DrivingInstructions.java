package org.firstinspires.ftc.teamcode.util;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class DrivingInstructions {
    public static void printDrivingInstructions(Telemetry telemetry){
        telemetry.addLine();
        telemetry.addLine("DRIVING INSTRUCTIONS");
        telemetry.addLine("Note: these instructions may be out of date, I don't update them often");
        telemetry.addLine();
        telemetry.addLine("Gamepad 1 controls:");
        telemetry.addLine("Driving: Left stick is translation, right stick x is rotation. Use the right trigger to slow down.");
        telemetry.addLine("calibrate feild-centric with the share button after you point the bot with the intake facing towards you");
        telemetry.addLine();
        telemetry.addLine("Lift and arm: toggle the claw open and closed with the left bumper, and toggle extend/retract with the left trigger");
        telemetry.addLine();
        telemetry.addLine("Gamepad 2:");
        telemetry.addLine("make adjustments to the lift extended height with dpad up and down.");
        telemetry.addLine("The height tweaks work even when the lift is down.");
        telemetry.addLine();
        telemetry.addLine();
        telemetry.addLine("There's more details to the exact behavior of the code that you'll learn as you drive, but I don't want to write them down");
    }
}