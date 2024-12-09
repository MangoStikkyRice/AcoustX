package jay.audio.roomaudio.model;

public class SoundSource {
    private double x;
    private double y;
    private double speedOfSound; 
    private double frequency; // Hz
    private double wavelength; // meters/pixels scale?

    public SoundSource(double x, double y, double speedOfSound, double frequency) {
        this.x = x;
        this.y = y;
        this.speedOfSound = speedOfSound;
        this.frequency = frequency;
        this.wavelength = speedOfSound / frequency;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSpeedOfSound() {
        return speedOfSound;
    }

    public double getFrequency() {
        return frequency;
    }

    public double getWavelength() {
        return wavelength;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
}