package amesmarket;

import java.io.IOException;
import java.util.List;

import amesmarket.extern.common.CommitmentDecision;
import amesmarket.filereaders.BadDataFileFormatException;

public interface SCUC {

	/**
	 *
	 * @param day
	 * @throws IOException
	 * @throws AMESMarketException
	 * @throws BadDataFileFormatException
	 */
	public abstract void calcSchedule(int day) throws IOException,
	AMESMarketException, BadDataFileFormatException;

	public abstract List<CommitmentDecision> getSchedule();

	/**
	 * Use a deterministic SCUC
	 */
	public static final int SCUC_DETERM = 0;
	/**
	 * Use a stochastic SCUC
	 */
	public static final int SCUC_STOC = 1;
}
