package pws.trainingData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class holds all of the training data and also handles the saving and loading of it
 */
@SuppressWarnings("ConstantConditions")
public class TrainingData {

	private final Random rand;
	private final List<InputOutputPair> data = new ArrayList<>();
	
	public TrainingData() {
		this(System.nanoTime());
	}
	
	private TrainingData(long seed) {
		rand = new Random(seed);
	}

    /**
     * Adds data to the trainingData, will get converted to an inputOutputPair
     * @param inputs The inputs
     * @param outputs The expected outputs
     */
	public void addData(float[] inputs, float[] outputs) {
		data.add(new InputOutputPair(inputs, outputs));
	}

    /**
     * Get a random data point from the trainingData
     * @return An InputOutputPair
     */
	public InputOutputPair getRandomDataPoint() {
		return data.get(rand.nextInt(data.size()));
	}
	
	public boolean hasData() {
		return data.size()>0;
	}

    /**
     * Adds a list of InputOutputPairs
     * @param data The list of InputOutputPairs
     */
	public void addData(List<InputOutputPair> data) {
		this.data.addAll(data);
	}

    /**
     * Reads the trainingData from a file
     * @param path The path to the folder containing the save
     * @param name The trainingdata name. trainingData will be added in front and .td will be added as extension.
     */
	public void readFromFile(Path path, String name) {
	    //Make sure the folder exists and is a folder
		if(!path.toFile().exists()) {
			System.out.println("No files found at " + path.toString());
			return;
		} else if(!path.toFile().isDirectory()) {
			System.err.println("Path should point to folder! It is currently pointing to: " + path.toAbsolutePath().toString());
		}

		//Get all of the files in the folder
		File[] filesInDir = path.toFile().listFiles();
		for(File f : filesInDir) {
			try {
			    //If the files name is correct we try to load it
				if(f.getName().matches("trainingData-"+name+"-.*.td")) {
					List<String> lines = Files.readAllLines(f.toPath());
					
					for(String s : lines) {
						if(s.length()==0) continue;
						//InputOutputPair has a fromString method we can use
						this.data.add(InputOutputPair.fromString(s));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Tried reading " + f.getAbsolutePath() + " but failed!");
			} catch (NumberFormatException e) {
				e.printStackTrace();
				System.err.println("Invalid data file!");
			}
		}
		
		System.out.println("Loaded " + this.data.size() + " data points!");
	}

    /**
     * Save the trainingData to a file, the name is the trainingData name and not the filename. "trainingdata-" will be added in front of the name.
     * The extension .td will also be added. If there is already a file it will add a - with a number to make the name unique
     * @param path The path to the folder containing the save
     * @param name The trainingdata name. trainingData will be added in front and .td will be added as extension.
     */
	public void writeToFile(Path path, String name) {
	    //Make sure there is data to save
		if(this.data.size()==0) {
			System.out.println("Nothing to save, returning!");
			return;
		}

		//Make sure the directory exist, and create it if needed
		if(!Files.isDirectory(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Could not create directory: " + path.toAbsolutePath() + "!");
				return;
			}
		}

		//Try and find an unique name, keep adding to i until an unique name is found
		Path filePath;
		int i = 0;
		do {
			filePath = path.resolve("trainingData-" + name + "-"+i+".td");
			i++;
		} while(Files.exists(filePath));

		//Convert all of the InputOutputPairs to strings
		List<String> lines = new ArrayList<>();
		lines.add("\n");
		for(InputOutputPair pair : this.data) {
			lines.add(pair.toString());
		}

		//Write all of the lines to the file
		try {
			Files.write(filePath, lines, StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not write to " + filePath.toAbsolutePath() + "!");
		}
	}
}
