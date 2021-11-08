package com.example.voicechanger;

import javax.sound.sampled.*;
import java.io.*;

class SimpleSoundPlayer
{
    public static void main(String[] args) throws FileNotFoundException {
        SimpleSoundPlayer sound = new SimpleSoundPlayer("src/Recording.wav");

        InputStream stream = new ByteArrayInputStream(sound.getSamples());

        EchoFilter filter = new EchoFilter(11025, 0.6f);

        stream = new FilteredSoundStream(stream, filter);

        sound.play(stream, 1f);

        System.exit(0);
    }

    private AudioFormat format;
    private byte[] samples;

    public SimpleSoundPlayer(String fileName)
    {
        try
        {
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(fileName));
            format = stream.getFormat();
            samples = getSamples(stream);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public byte[] getSamples()
    {
        return samples;
    }

    private byte[] getSamples(AudioInputStream stream)
    {
        int length = (int) stream.getFrameLength() * format.getFrameSize();

        byte[] samples = new byte[length];
        DataInputStream data = new DataInputStream(stream);

        try
        {
            data.readFully(samples);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return samples;
    }

    public void play(InputStream source, float pitch)
    {
        int bufferSize = format.getFrameSize() * Math.round(format.getSampleRate()/10);
        byte[] buffer = new byte[bufferSize];

        AudioFormat newFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate()*pitch, 16, format.getChannels(), format.getChannels() * 2, format.getSampleRate(), false);

        SourceDataLine line = null;
        try
        {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, newFormat);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(newFormat, bufferSize);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        line.start();

        try
        {
            int numBytesRead = 0;
            while(numBytesRead != -1)
            {
                numBytesRead = source.read(buffer, 0, buffer.length);
                if(numBytesRead != -1)
                {
                    line.write(buffer, 0, numBytesRead);
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        line.drain();
        line.close();
    }
}

abstract class SoundFilter
{
    public void reset() {}

    public int getRemainingSize()
    {
        return 0;
    }
    public void filter(byte[] samples)
    {
        filter(samples, 0, samples.length);
    }

    public abstract void filter(byte[] samples, int offset, int length);

    public static short getSample(byte[] buffer, int position)
    {
        return (short) (((buffer[position + 1] & 0xff) << 8) | (buffer[position] & 0xff));
    }

    public static void setSample(byte[] buffer, int position, short samples)
    {
        buffer[position] = (byte) (samples & 0xff);
        buffer[position + 1] = (byte) ((samples >> 8) & 0xff);
    }
}

class FilteredSoundStream extends FilterInputStream
{
    private static final int REMAINING_SIZE_UNKNOWN = -1;
    private  SoundFilter soundFilter;
    private  int remainingSize;

    public FilteredSoundStream(InputStream in, SoundFilter soundFilter) throws FileNotFoundException {
        super(in);
        this.soundFilter = soundFilter;
        remainingSize = REMAINING_SIZE_UNKNOWN;
    }

    public int read(byte[] samples, int offset, int length) throws IOException
    {
        int bytesRead = super.read(samples, offset, length);
        if (bytesRead > 0)
        {
            soundFilter.filter(samples, offset, bytesRead);
            return bytesRead;
        }

        if (remainingSize == REMAINING_SIZE_UNKNOWN)
        {
            remainingSize = soundFilter.getRemainingSize();
            remainingSize = remainingSize / 4 * 4;
        }

        if (remainingSize > 0)
        {
            length = Math.min(length, remainingSize);

            for(int i = offset; i < offset + length; i++)
            {
                samples[i] = 0;
            }

            soundFilter.filter(samples, offset, length);
            remainingSize -= length;

            return length;
        }
        else
        {
            return -1;
        }
    }
}

class EchoFilter extends SoundFilter
{
    private  short[] delayBuffer;
    private int delayBufferPos;
    private float decay;

    public EchoFilter(int numDelaySamples, float decay)
    {
        delayBuffer = new short[numDelaySamples];
        this.decay = decay;
    }

    public int getRemainingSize()
    {
        float finalDecay = 0.01f;
        int numRemainingBuffers = (int) Math.ceil(Math.log(finalDecay) / Math.log(decay));
        int bufferSize = delayBuffer.length * 2;

        return bufferSize * numRemainingBuffers;
    }

    public void reset()
    {
        for (int i = 0; i < delayBuffer.length; i++)
        {
            delayBuffer[i] = 0;
        }
        delayBufferPos = 0;
    }

    @Override
    public void filter(byte[] samples, int offset, int length) {
        for (int i = offset; i < offset + length; i += 2)
        {
            short oldSample = getSample(samples, i);
            short newSample = (short) (oldSample + decay * delayBuffer[delayBufferPos]);

            setSample(samples, i, newSample);

            delayBuffer[delayBufferPos] = newSample;
            delayBufferPos++;

            if (delayBufferPos == delayBuffer.length)
            {
                delayBufferPos = 0;
            }
        }
    }
}

