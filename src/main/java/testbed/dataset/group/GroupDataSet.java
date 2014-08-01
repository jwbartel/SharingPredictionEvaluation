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

import recommendation.groups.seedless.hybrid.IOFunctions;
import testbed.dataset.DataSet;
import util.tools.io.CollectionIOAssist;
import util.tools.io.ValueParser;

public abstract class GroupDataSet<Id, Collaborator> extends DataSet<Id> {
	ValueParser<Collaborator> parser;
	Class<Collaborator> collaboratorClass;
	IOFunctions<Collaborator> ioHelp;
	
	
	public GroupDataSet(String name, Id[] accountIds, File rootFolder, Class<Collaborator> collaboratorClass, ValueParser<Collaborator> parser) {
		super(name, accountIds, rootFolder);
		this.parser = parser;
		this.collaboratorClass = collaboratorClass;
		this.ioHelp = new IOFunctions<>(collaboratorClass);
	}

	public UndirectedGraph<Collaborator, DefaultEdge> getGraph(Id account) {
		return ioHelp.createUIDGraph(getGraphFile(account).getAbsolutePath());
	}


	public Collection<Set<Collaborator>> getIdealGroups(Id account) {
		return ioHelp.loadIdealGroups(getIdealGroupsFile(account).getAbsolutePath());
	}

	public Collection<Set<Collaborator>> getMaximalCliques(Id account) {
		File maximalCliquesFile = getMaximalCliquesFile(account);
		if (!maximalCliquesFile.exists()) {
			BronKerboschCliqueFinder<Collaborator, DefaultEdge> cliqueFinder = new BronKerboschCliqueFinder<>(
					getGraph(account));
			Collection<Set<Collaborator>> maximalCliques = cliqueFinder.getAllMaximalCliques();
			ioHelp.printCliqueIDsToFile(maximalCliquesFile.getAbsolutePath(), maximalCliques);
		}
		return ioHelp.loadCliqueIDs(getMaximalCliquesFile(account)
				.getAbsolutePath());
	}

	public void writeGroupPredictions(String predictionType, Id account,
			Collection<Set<Collaborator>> predictions) {
		
		File outFile = getPredictedGroupsFile(predictionType, account);
		ioHelp.printCliqueIDsToFile(outFile.getAbsolutePath(), predictions);
		
	}

	public Set<Collaborator> getNewMembers(Id account, double growthRate, int test) {
		try {
			File newMembersFile = getNewMembersFile(account, growthRate, test);
			if (!newMembersFile.exists()) {
				if (!newMembersFile.getParentFile().exists()) {
					newMembersFile.getParentFile().mkdirs();
				}
				MembershipChangeFinder<Collaborator> changeFinder = new MembershipChangeFinder<>();
				Set<Collaborator> newMembers = changeFinder
						.getPseudoRandomNewIndividuals(getGraph(account)
								.vertexSet(), growthRate);
				CollectionIOAssist.writeCollection(newMembersFile, newMembers);
			}
			Set<Collaborator> newMembers = new HashSet<>(
					CollectionIOAssist.readCollection(newMembersFile, parser));
			return newMembers;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public abstract File getGraphFile(Id account);

	public abstract File getIdealGroupsFile(Id account);

	public abstract File getSubstepsFolder(Id account);

	public abstract File getPredictedGroupsFile(String predictionType, Id account);
	
	public abstract File getSeedlessMetricsFile();
	
	public abstract File getNewMembersFile(Id account, double growthRate, int test);
	
	public abstract File getEvolutionMetricsFile();

	public abstract File getMaximalCliquesFile(Id account);

}