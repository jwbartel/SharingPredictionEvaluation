package testbed.dataset.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import recommendation.groups.seedless.hybrid.IOFunctions;
import testbed.dataset.DataSet;
import data.representation.actionbased.CollaborativeAction;
import data.representation.actionbased.CollaborativeActionThread;

public abstract class ActionsDataSet<IdType, CollaboratorType, ActionType extends CollaborativeAction<CollaboratorType>, ThreadType extends CollaborativeActionThread<CollaboratorType, ActionType>>
		extends DataSet<IdType> {


	public static class ThreadFold<CollaboratorType, ActionType extends CollaborativeAction<CollaboratorType>, ThreadType extends CollaborativeActionThread<CollaboratorType, ActionType>> {
		public final Collection<ThreadType> trainThreads;
		public final Collection<ThreadType> validationThreads;
		public final Collection<ThreadType> testThreads;

		public ThreadFold(Collection<ThreadType> trainThreads,
				Collection<ThreadType> validationThreads,
				Collection<ThreadType> testThreads) {
			this.trainThreads = trainThreads;
			this.validationThreads = validationThreads;
			this.testThreads = testThreads;
		}
	}

	public ActionsDataSet(String name, IdType[] accountIds, File rootFolder,
			Class<IdType> genericClass) {
		super(name, accountIds, rootFolder, genericClass);
	}
	
	public Map<Integer, ThreadFold<CollaboratorType, ActionType, ThreadType>> getThreadFolds(
			IdType account, int numFolds) {

		List<ThreadType> threads = new ArrayList<>(getAllThreads(account));
		List<Collection<ThreadType>> folds = new ArrayList<>(numFolds);
		
		int foldSize = threads.size() / numFolds;
		if (foldSize == 0) {
			return new TreeMap<>();
		}
		int remainder = threads.size() % foldSize;
		
		int pos = 0;
		int foldNum = 0;
		while (pos < threads.size() && foldNum < numFolds) {
			
			Collection<ThreadType> fold = new ArrayList<>();
			for (int i=0; i< foldSize; i++) {
				fold.add(threads.get(pos));
				pos++;
			}
			if (remainder > 0) {
				fold.add(threads.get(pos));
				pos++;
				remainder--;
			}
			folds.add(fold);
			foldNum++;
		}

		Map<Integer, ThreadFold<CollaboratorType, ActionType, ThreadType>> retVal =
				new TreeMap<>();
		int validationPosition = folds.size() - 2;
		int testPostion = folds.size()-1;
		for (int foldId=0; foldId<folds.size(); foldId++) {

			Collection<ThreadType> trainThreads = new ArrayList<>();
			Collection<ThreadType> validationThreads = new ArrayList<>(
					folds.get(validationPosition));
			Collection<ThreadType> testThreads = new ArrayList<>(
					folds.get(testPostion));
			

			for (int j = 0; j < folds.size(); j++) {
				if (j != validationPosition && j != testPostion) {
					trainThreads.addAll(folds.get(j));
				}
			}
			
			validationPosition = (validationPosition + 1) % folds.size();
			testPostion = (testPostion + 1) % folds.size();
			

			retVal.put(foldId, new ThreadFold<>(trainThreads, validationThreads, testThreads));
		}
		return retVal;
	}
	
	private File getGroupsFolder(IdType account) {
		File folder = new File(getRootFolder(), "groups");
		if (account != null) {
			return new File(folder, account.toString());
		} else {
			return folder;
		}
	}
	
	private File getArgumentlessGraphBasedFolder(IdType account,
			String graphBuilderType) {
		File groupsFolder = getGroupsFolder(account);
		groupsFolder = new File(groupsFolder, graphBuilderType);
		return groupsFolder;
	}
	
	public File getArgumentlessGraphBasedGroupsFile(IdType account,
			String graphBuilderType) {
		File groupsFolder = getArgumentlessGraphBasedFolder(account,
				graphBuilderType);
		return new File(groupsFolder, "groups.txt");
	}
	
	public File getArgumentlessGraphBasedGraphFile(IdType account,
			String graphBuilderType) {
		File groupsFolder = getArgumentlessGraphBasedFolder(account,
				graphBuilderType);
		return new File(groupsFolder, "graph.txt");
	}
	
	public File getTimeThresholdGraphBasedFolder(IdType account,
			String graphBuilderType, String timeThreshold) {
		File groupsFolder = getTimeThresholdGraphBasedFolder(account,
				graphBuilderType, timeThreshold);
		return groupsFolder;
	}

	public File getTimeThresholdGraphBasedGroupsFile(IdType account,
			String graphBuilderType, String timeThreshold) {
		File groupsFolder = getGroupsFolder(account);
		groupsFolder = new File(groupsFolder, graphBuilderType);
		groupsFolder = new File(groupsFolder, timeThreshold);
		return new File(groupsFolder, "groups.txt");
	}

	public File getTimeThresholdGraphBasedGraphFile(IdType account,
			String graphBuilderType, String timeThreshold) {
		File groupsFolder = getGroupsFolder(account);
		groupsFolder = new File(groupsFolder, graphBuilderType);
		groupsFolder = new File(groupsFolder, timeThreshold);
		return new File(groupsFolder, "graph.txt");
	}
	
	protected File getAnalysisFolder() {
		return new File(getRootFolder(), "analysis");
	}
	
	private File getEdgeWeightsAnalysisFolder() {
		return new File(getAnalysisFolder(), "edge weights");
	}
	
	private File getFixedWOutsEdgeWeightsAnalysisFolder() {
		return new File(getEdgeWeightsAnalysisFolder(), "fixed wOut");
	}
	
	private File getFixedWoutsEdgeWeightsAnalysisFile(String halfLife) {
		return new File(getFixedWOutsEdgeWeightsAnalysisFolder(), halfLife + ".csv");
	}
	
	private File getFixedHalfLivesEdgeWeightsAnalysisFolder(String halfLife) {
		return new File(getEdgeWeightsAnalysisFolder(), "fixed halflife "+halfLife);
	}
	
	private File getFixedHalfLivesEdgeWeightsAnalysisFile(String halfLife, String wOut) {
		return new File(getFixedHalfLivesEdgeWeightsAnalysisFolder(halfLife), wOut);
	}


	public File getScoredEdgesGraphBasedFolder(IdType account,
			String graphBuilderType,
			String halfLife,
			double wOut,
			double scoreThreshold) {
		File groupsFolder = getGroupsFolder(account);
		groupsFolder = new File(groupsFolder, graphBuilderType);
		groupsFolder = new File(groupsFolder, "halfLife-" + halfLife);
		groupsFolder = new File(groupsFolder, "wOut-" + wOut);
		groupsFolder = new File(groupsFolder, "scoreThreshold-" + scoreThreshold);
		return groupsFolder;
	}

	public File getScoredEdgesGraphBasedGraphFile(IdType account,
			String graphBuilderType,
			String halfLife,
			double wOut,
			double scoreThreshold) {
		File groupsFolder = getScoredEdgesGraphBasedFolder(account,
				graphBuilderType, halfLife, wOut, scoreThreshold);
		return new File(groupsFolder, "graph.txt");
	}

	public File getScoredEdgesGraphBasedGroupsFile(IdType account,
			String graphBuilderType,
			String halfLife,
			double wOut,
			double scoreThreshold) {
		File groupsFolder = getScoredEdgesGraphBasedFolder(account,
				graphBuilderType, halfLife, wOut, scoreThreshold);
		return new File(groupsFolder, "groups.txt");
	}

	public Collection<Set<CollaboratorType>> loadGroups(File groupsFile,
			IOFunctions<CollaboratorType> ioHelp) {

		return ioHelp.loadCliqueIDs(groupsFile.getAbsolutePath());
	}

	public List<Double> getEdgeWeights(SimpleWeightedGraph<CollaboratorType, DefaultWeightedEdge> graph) {
		List<Double> weights = new ArrayList<>(graph.edgeSet().size());
		for (DefaultWeightedEdge edge : graph.edgeSet()) {
			Double weight = graph.getEdgeWeight(edge);
			weights.add(weight);
		}
		return weights;
	}

	public SimpleWeightedGraph<CollaboratorType, DefaultWeightedEdge> loadGraph(
			File graphFile) throws IOException {
		SimpleWeightedGraph<CollaboratorType, DefaultWeightedEdge> graph = 
				new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

		List<String> lines = FileUtils.readLines(graphFile);
		for (String line : lines) {
			String[] parts = line.split("\t");

			CollaboratorType collaborator1 = parseCollaborator(parts[0]);
			if (!graph.containsVertex(collaborator1)) {
				if(!graph.addVertex(collaborator1)){
					throw new RuntimeException(collaborator1+" not added");
				}
			}

			CollaboratorType collaborator2 = parseCollaborator(parts[1]);
			if (!graph.containsVertex(collaborator2)) {
				if (!graph.addVertex(collaborator2)) {
					throw new RuntimeException(collaborator2+" not added");
				}
			}

			if (!graph.containsEdge(collaborator1, collaborator2)) {
				double weight = Double.parseDouble(parts[2]);
				DefaultWeightedEdge edge = new DefaultWeightedEdge();
				if (!graph.addEdge(collaborator1, collaborator2, edge)) {
					throw new RuntimeException("did not add edge between "
							+ collaborator1 + " and " + collaborator2);
				}
				graph.setEdgeWeight(edge, weight);
			}
		}
		return graph;
	}
	
	public List<IdType> sampleAccounts(int sampleSize) {
		
		IdType[] accounts = getAccountIds();
		if (accounts.length <= sampleSize) {
			return new ArrayList<>(Arrays.asList(accounts));
		}
		
		Set<Integer> usedIndexes = new TreeSet<>();
		Random rand = new Random();
		List<IdType> sampleAccounts = new ArrayList<>();
		while(sampleAccounts.size() < sampleSize) {
			int index = rand.nextInt(accounts.length);
			while (usedIndexes.contains(index)) {
				index = rand.nextInt(accounts.length);
			}
			
			sampleAccounts.add(accounts[index]);
			usedIndexes.add(index);
		}
		
		return sampleAccounts;
		
	}
	
	public void writeEdgeWeightsWithFixedHalfLife(String halfLife, int sampleSize) throws IOException {
		
		for (IdType account : sampleAccounts(sampleSize)) {
			File groupsFolder = getGroupsFolder(account);
			File interactionRankFolder = new File(groupsFolder, "Interaction Rank");

			File halfLifeFolder = new File(interactionRankFolder, "halfLife-"+halfLife);
			if (!halfLifeFolder.exists()) {
				continue;
			}
			for (File wOutFolder : halfLifeFolder.listFiles()) {
				if (!wOutFolder.isDirectory()) {
					continue;
				}
				
				String wOut = wOutFolder.getName();
				File graphFile = new File(new File(wOutFolder, "scoreThreshold-0.0"), "graph.txt");

				if (graphFile.exists()) {
					System.out.println("\t\t"+wOut);
					
					SimpleWeightedGraph<CollaboratorType, DefaultWeightedEdge> graph =
							loadGraph(graphFile);
					List<Double> weights = getEdgeWeights(graph);
					String outputStr = "";
					for (Double weight : weights) {
						outputStr += weight + "\n";
					}
					FileUtils.write(getFixedHalfLivesEdgeWeightsAnalysisFile(halfLife, wOut),
							outputStr, true);
				}
			}
		}
	}
	
	public void writeEdgeWeightsWithFixedWOut(int sampleSize) throws IOException {
		
		for (IdType account : sampleAccounts(sampleSize)) {
			System.out.println(account);
			File groupsFolder = getGroupsFolder(account);
			File interactionRankFolder = new File(groupsFolder, "Interaction Rank");
			for (File halfLifeFolder : interactionRankFolder.listFiles()) {
				if (!halfLifeFolder.isDirectory()) {
					continue;
				}
				
				String halfLife = halfLifeFolder.getName();

				File graphFile = new File(new File(new File(halfLifeFolder,
						"wOut-1.0"), "scoreThreshold-0.0"), "graph.txt");
				if (graphFile.exists()) {
					System.out.println("\t"+halfLife);
					

					
					SimpleWeightedGraph<CollaboratorType, DefaultWeightedEdge> graph =
							loadGraph(graphFile);
					String outputStr = "";
					for (Double weight : getEdgeWeights(graph)) {
						outputStr += weight + "\n";
					}
					FileUtils.write(getFixedWoutsEdgeWeightsAnalysisFile(halfLife),
							outputStr, true);
				}
			}
		}
	}
	
	public abstract Collection<ActionType> getAllMessages(IdType account);
	public abstract Collection<ActionType> getTrainMessages(IdType account, double percentTrain);
	public abstract Collection<ActionType> getTestMessages(IdType account, double percentTrain);
	
	public abstract Collection<ThreadType> getAllThreads(IdType account);
	public abstract Collection<ThreadType> getTrainThreads(IdType account, double percentTrain);
	public abstract Collection<ThreadType> getTestThreads(IdType account, double percentTest);
	
	public abstract File getActionBasedSeedlessGroupsMetricsFile();
	public abstract File getBurstyGroupsMetricsFile();
	public abstract File getEvolutionMetricsFile();

	public abstract CollaboratorType parseCollaborator(String collaboratorStr);
}
