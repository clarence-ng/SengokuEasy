package nexi.sengoku.easy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

public class SengokuEasy {

	private static final Logger logger = Logger.getLogger(SengokuEasy.class);

	public static final String propertiesFilePath = "sengoku.properties";

	private final Auth auth;
	private final WebClient webClient;
	private final Properties properties;
	private World world;
	private volatile Context context;

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
		
		auth = new Auth(properties);
		webClient = Client.newWebClient();
	}

	public void run(String... args) throws FileNotFoundException, IOException, ElementNotFoundException, FailingHttpStatusCodeException, WeAreBrokenException {
		System.out.println("Weclome to SengokuEasy");
		Scanner scanner = new Scanner(System.in);
		scanner.useDelimiter(Pattern.compile("\n"));
		for (;;) {
			handle(scanner.next());
		}
	}

	private void handle(String next) {
		String[] args = next.split("\\s");
		if (args.length <= 0) {
			help();

		} else {
			String[] remainingArgs;
			if (args.length > 1) {
				remainingArgs = Arrays.asList(args).subList(1, args.length).toArray(new String[0]);
			} else {
				remainingArgs = new String[0];
			}

			String command = args[0];

			try {
				if (command.equalsIgnoreCase("help")) {
					help();
				} else if (command.equalsIgnoreCase("exit")) {
					System.exit(0);
				} else if (command.equalsIgnoreCase("go")) {
					world = World.getInstance();
					parseBean(world, remainingArgs);
					context = new Context(world.getId(), auth, webClient, properties);
					world.load(context);
				} else if (command.equalsIgnoreCase("dm")) {
					DoMissionTask.Args commandArgs = new DoMissionTask.Args();
					parseBean(commandArgs, remainingArgs);
					DoMissionTask dm = new DoMissionTask(context, commandArgs.villageId, commandArgs.mission);
					if (commandArgs.repeat) {
						for (;;) {
							dm.run();
							Thread.sleep(5000L);
						}
					} else {
						dm.run();
					}
				}
				else if (command.equalsIgnoreCase("ug")) {
					DoUpgradeTask.Args commandArgs = new DoUpgradeTask.Args();
					parseBean(commandArgs, remainingArgs);
					DoUpgradeTask du = new DoUpgradeTask(context,
							world.getVillageFromIndex(commandArgs.villageIndex)
								.getVillageMap().getTile(commandArgs.x, commandArgs.y),
							world.getVillageIdFromIndex(commandArgs.villageIndex)
						);
					du.run();
				} 
				else if (command.equalsIgnoreCase("lv")) {
					world.listVillages();
				} 
				else if (command.equalsIgnoreCase("pv")) {
					World.Args commandArgs = new World.Args();
					parseBean(commandArgs, remainingArgs);
					logger.info("Index = " + commandArgs.villageIndex);
					world.getVillageFromIndex(commandArgs.villageIndex).displayVillageMap();
				} 
				else if (command.equalsIgnoreCase("pr")){
					world.printResource();
				}
				else {
					System.out.println("unknown command.");
					help();
				}
			} catch(CmdLineException e) {
			} catch(Exception e) {
				logger.info("An error occured.", e);
			}
		}
	}

	private void parseBean(Object bean, String[] args) throws CmdLineException {
		final CmdLineParser parser = new CmdLineParser(bean);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			logger.error(e);
			throw e;
		}
	}

	public void help() {
		StringBuilder sb = new StringBuilder();
		sb.append("Available commands:\n");
		sb.append("help :prints this\n");
		sb.append("exit :exit program\n");
		sb.append("go :goes to the world. Must be called prior to calling other commands. [e.g. go -w 1]\n");
		sb.append("dm :looks at mission page and sends team to mission. [e.g. dm -hp 81 -r]\n");
		sb.append("pr :prints resource on screen. [e.g. pv]\n");
		sb.append("lv :list the villages you have. [e.g. lv]\n");
		sb.append("pv :prints map of a specified village. [e.g. pv -index 0]\n");
		sb.append("ug :upgrades a building. [e.g. ug -index -0 -x 1 -y 1]\n");
		System.out.println(sb.toString());
	}

	public static void debug(HtmlElement element, int tabs, Logger logger) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < tabs; i++) {
			builder.append("\t"); 
		}
		logger.debug(builder.toString() + "id:" + element.getId() 
				+ " type " + element.getClass().getSimpleName() 
				+ " class " + element.getAttribute("class"));
		for (HtmlElement child : element.getChildElements()) {	
			debug(child, tabs+1, logger);
		}
	}
}
