package testbed.dataset.group;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import model.tools.evolution.MembershipChangeFinder;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.graph.DefaultEdge;

import testbed.dataset.DataSet;
import util.tools.io.CollectionIOAssist;
import util.tools.io.IntegerValueParser;

public abstract class GroupDataSet<V> extends DataSet<V> {
	public GroupDataSet(String name, V[] accountIds, File rootFolder, Class<V> genericClass) {
		super(name, accountIds, rootFolder, genericClass);
	}

	public UndirectedGraph<V, DefaultEdge> getGraph(V account) {
		return ioHelp.createUIDGraph(getGraphFile(account).getAbsolutePath());
	}


	public Collection<Set<V>> getIdealGroups(V account) {
		return ioHelp.loadIdealGroups(getIdealGroupsFile(account).getAbsolutePath());
	}

	public Collection<Set<V>> getMaximalCliques(V account) {
		File maximalCliquesFile = getMaximalCliquesFile(account);
		if (!maximalCliquesFile.exists()) {
			BronKerboschCliqueFinder<V, DefaultEdge> cliqueFinder = new BronKerboschCliqueFinder<>(
					getGraph(account));
			Collection<Set<V>> maximalCliques = cliqueFinder.getAllMaximalCliques();
			ioHelp.printCliqueIDsToFile(maximalCliquesFile.getAbsolutePath(), maximalCliques);
		}
		return ioHelp.loadCliqueIDs(getMaximalCliquesFile(account)
				.getAbsolutePath());
	}

	public void writeGroupPredictions(String predictionType, V account,
			Collection<Set<V>> predictions) {
		
		File outFile = getPredictedGroupsFile(predictionType, account);
		ioHelp.printCliqueIDsToFile(outFile.getAbsolutePath(), predictions);
		
	}

	public Set<Integer> getNewMembers(V account, double growthRate, int test) {
		try {
			File newMembersFile = getNewMembersFile(account, growthRate, test);
			if (!newMembersFile.exists()) {
				if (!newMembersFile.getParentFile().exists()) {
					newMembersFile.getParentFile().mkdirs();
				}
				MembershipChangeFinder<V> changeFinder = new MembershipChangeFinder<>();
				Set<V> newMembers = changeFinder
						.getPseudoRandomNewIndividuals(getGraph(account)
								.vertexSet(), growthRate);
				CollectionIOAssist.writeCollection(newMembersFile, newMembers);
			}
			Set<Integer> newMembers = new HashSet<>(
					CollectionIOAssist.readCollection(newMembersFile,
							new IntegerValueParser()));
			return newMembers;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public abstract File getGraphFile(V account);

	public abstract File getIdealGroupsFile(V account);

	public abstract File getSubstepsFolder(V account);

	public abstract File getPredictedGroupsFile(String predictionType, V account);
	
	public abstract File getSeedlessMetricsFile();
	
	public abstract File getNewMembersFile(V account, double growthRate, int test);
	
	public abstract File getEvolutionMetricsFile();

	public abstract File getMaximalCliquesFile(V account);

}