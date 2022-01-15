package net.sourceforge.jaad.aac;

import net.sourceforge.jaad.aac.syntax.BitStream;
import net.sourceforge.jaad.aac.syntax.PCE;
import net.sourceforge.jaad.aac.syntax.SyntacticElements;
import net.sourceforge.jaad.aac.transport.ADIFHeader;

import javax.sound.sampled.AudioFormat;
import java.nio.ShortBuffer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main AAC decoder class
 * @author in-somnia
 */
public class Decoder {

	static final Logger LOGGER = Logger.getLogger("jaad.aac.Decoder"); //for debugging

	private final DecoderConfig config;
	private final SyntacticElements syntacticElements;
	public int frames=0;
	private ADIFHeader adifHeader;

	/**
	 * The methods returns true, if a profile is supported by the decoder.
	 * @param profile an AAC profile
	 * @return true if the specified profile can be decoded
	 * @see Profile#isDecodingSupported()
	 */
	public static boolean canDecode(Profile profile) {
		return profile.isDecodingSupported();
	}

	public static DecoderConfig createConfig(byte[] data) {
		return new DecoderConfig().decode(BitStream.open(data));
	}

	public static Decoder create(byte[] data) {
		return create(BitStream.open(data));
	}

	public static Decoder create(int objectType, int frequency, int channels) {
		byte[] config = DecoderConfig.encodeConfiguration(objectType, frequency, channels);
		return create(BitStream.open(config));
	}

	public static Decoder create(BitStream in) {
		DecoderConfig config = new DecoderConfig().decode(in);
		return create(config);
	}

	public static Decoder create(AudioDecoderInfo info) {
		DecoderConfig config = DecoderConfig.create(info);
		return create(config);
	}

	public static Decoder create(DecoderConfig config) {
		if(config==null)
			throw new IllegalArgumentException("illegal MP4 decoder specific info");
		return new Decoder(config);
	}

	/**
	 * Initializes the decoder with a MP4 decoder specific info.
	 *
	 * After this the MP4 frames can be passed to the
	 * <code>decodeFrame(byte[], SampleBuffer)</code> method to decode them.
	 * 
	 * @param config decoder specific info from an MP4 container
	 * @throws AACException if the specified profile is not supported
	 */
	private Decoder(DecoderConfig config) {
		//config = DecoderConfig.parseMP4DecoderSpecificInfo(decoderSpecificInfo);

		this.config = config;

		syntacticElements = new SyntacticElements(config);

		LOGGER.log(Level.FINE, "profile: {0}", config.getProfile());
		LOGGER.log(Level.FINE, "sf: {0}", config.getSampleFrequency().getFrequency());
		LOGGER.log(Level.FINE, "channels: {0}", config.getChannelConfiguration().getDescription());
	}

	public DecoderConfig getConfig() {
		return config;
	}

	/**
	 * Decodes one frame of AAC data in frame mode and returns the raw PCM
	 * data.
	 * @param frame the AAC frame
	 * @throws AACException if decoding fails
	 */

	public ShortBuffer decodeFrame(byte[] frame) {
		BitStream in = BitStream.open(frame);

		try {
			LOGGER.log(Level.FINE, ()->String.format("frame %d @%d", frames, 8*frame.length));
			return decode(in);
		} catch(EOSException e) {
			LOGGER.log(Level.WARNING,"unexpected end of frame",e);
		} finally {
			++frames;
		}

		return null;
	}

	private ShortBuffer decode(BitStream in) {
		if(ADIFHeader.isPresent(in)) {
			adifHeader = ADIFHeader.readHeader(in);
			PCE pce = adifHeader.getFirstPCE();
			config.setAudioDecoderInfo(pce);
		}

		if(!canDecode(config.getProfile()))
			throw new AACException("unsupported profile: "+config.getProfile().getDescription());

		syntacticElements.startNewFrame();

		//1: bitstream parsing and noiseless coding
		syntacticElements.decode(in);
		//2: spectral processing
		List<float[]> channels = syntacticElements.process();
		//3: send to output buffer
		return syntacticElements.sendToOutput(channels, config.getSampleLength());
	}

	public AudioFormat getAudioFormat() {

		int freq = config.getSampleFrequency().getFrequency();

		// assume SBR/PS
		if(!config.getProfile().isErrorResilientProfile()
				&& config.getChannelConfiguration()==ChannelConfiguration.MONO
				&& freq < 24000)
			freq *= 2;

		return new AudioFormat(freq,16, config.getChannelCount(), true, false);
	}
}
