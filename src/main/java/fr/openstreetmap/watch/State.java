package fr.openstreetmap.watch;

public class State {
	public static String downloading;
	public static long lastDiscoveredId;
	public static long currentDownloadQueue;
	
	public static int downloadedDiffs;
	public static long totalDownloadSize;
	public static long totalDownloadTime;
	
	public static boolean processing;
	public static long totalProcessingTime;
	public static long parsingProcessingTime;
	
	public static int processedChangesets;
	public static int emittedMatches;
	public static int matchedChangesets;
}
