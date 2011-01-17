package nexi.sengoku.easy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nexi.sengoku.easy.BuildArmyTask.UnitType;
import nexi.sengoku.easy.Context.MasterContext;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.CmdLineParser;

public class SengokuEasy {

	private static final Logger logger = Logger.getLogger(SengokuEasy.class);

	public static final String propertiesFilePath = "sengoku.properties";

	private final Properties properties;

	private final ThreadLocal<MasterContext> masterContext = new ThreadLocal<MasterContext>() {
		@Override 
		protected MasterContext initialValue() {
			return new MasterContext();
		}
	};

	private final ExecutorService exeuctor = Executors.newCachedThreadPool();

	public static void main (String... args) throws Exception {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.ERROR);
		Logger.getLogger("org.apache.http").setLevel(Level.ERROR);
		new SengokuEasy().run(args);
	}

	public SengokuEasy() throws FileNotFoundException, IOException {
		File propertiesFile = new File(propertiesFilePath);
		properties = new Properties();
		properties.load(new FileReader(propertiesFile));
	}

	public void run(String... args) throws Exception {
		System.out.println("Weclome to SengokuEasy");

		if (args.length < 1) {
			System.out.println("Must provide an argument [-auto, -enemy -war]");
			System.exit(-1);
		}
		if (args[0].contains("auto")) {
			System.out.println("Running auto mode");

			final AutoArgs arg = new AutoArgs();
			CmdLineParser parser = new CmdLineParser(arg);
			parser.parseArgument(args);

			if (arg.debug) {
				Logger.getRootLogger().setLevel(Level.DEBUG);
			}

			exeuctor.execute(new Runnable() {
				@Override
				public void run() {
					for (;;) {
						Context context = masterContext.get().newContext(arg.world);
						try {
							boolean success = true;
							if (arg.buildArmy.length() > 0) {
								String[] types = arg.buildArmy.split(",");
								List<UnitType> typesToTrain = new ArrayList<UnitType>();
								for (String s : types) {
									if (!BuildArmyTask.unitTypes.containsKey(s)) {
										throw new IllegalArgumentException("Invalid unit type:" + s);
									}
									typesToTrain.add(BuildArmyTask.unitTypes.get(s));
								}

								String[] v = arg.buildArmyVillageIds.split(",");
								List<String> villageIds = new ArrayList<String>();
								for (String s : v) {
									villageIds.add(s.trim());
								}
								success = new BuildArmyTask(context, typesToTrain, villageIds).call();
							}
							if (arg.buildStructure && success) {
								new BuildStructureTask(context).call();
							}
							if (arg.doMission) {
								new DoMissionTask(context, arg.doMissionVillage, Mission.SpringOfLongevity).call();
							}
							if (arg.arrangeArmy) {
								new ArrangeArmyTask(context).call();
							}
						} catch (Exception e) {
						} finally {
							context.webClient.closeAllWindows();
						}
						try {
							Thread.sleep(1000 * 60 * 3);
						} catch (InterruptedException e) {

						}

						if (arg.reportStatus) {
							exeuctor.execute(new Runnable() {
								@Override
								public void run() {
									Context context = masterContext.get().newContext(15L);
									try {
										new StatusTask(context).call();
									} catch (Exception e) {
									} finally {
										context.webClient.closeAllWindows();
									}
								}
							});
						}
					}
				}
			});

		} else if (args[0].contains("enemy")) {
			EnemyArgs arg = new EnemyArgs();
			arg.world = 15;
			arg.country = "5";
			arg.coordinates = "-36,-36";
			arg.mainCoordinates = "-62,-117";

			List<Coordinates> coordinates = new ArrayList<Coordinates> ();
			coordinates.add(Coordinates.parse(arg.coordinates));
			new MapTask(masterContext.get().newContext(arg.world), arg.world, coordinates, Coordinates.parse(arg.mainCoordinates)).call();
		} else if (args[0].contains("war")) {
			WarArgs taskArgs = new WarArgs();
			CmdLineParser p = new CmdLineParser(taskArgs);
			p.parseArgument(args);	
			new WarReportTask(taskArgs.world).call();
		}
	}

	private class AutoArgs {
		@Option(name="-auto", required=true)
		boolean auto;
		@Option(name="-w", required=true)
		long world;
		@Option(name="-rs")
		boolean reportStatus = false;

		@Option(name="-dm")
		boolean doMission = false;
		@Option(name="-dmV")
		String doMissionVillage ="";

		@Option(name="-ba")
		String buildArmy = "";
		@Option(name="-baV")
		String buildArmyVillageIds ="";

		@Option(name="-bs")
		boolean buildStructure = false;
		@Option(name="-aa")
		boolean arrangeArmy = false;

		@Option(name="-debug")
		boolean debug = false;
	}

	private class EnemyArgs {
		@Option(name="-enemy", required=true)
		boolean enemy;
		@Option(name="-w", required=true)
		long world;
		@Option(name="-c", required=true)
		String country;
		@Option(name="-x", required=true)
		String coordinates;
		@Option(name="-mx", required=true)
		String mainCoordinates;
	}

	private class WarArgs {
		@Option(name="-w", required=true)
		long world;
		@Option(name="-war", required=true)
		boolean auto;
	}
}
