package ai;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 
 * @author Naveen confederacy always uses Greedy Algo Union player always makes
 *         the first move from the configuration provided Moves: 1. Paratroop
 *         drop : open space anywhere -> creates a new piece 2. Force march :
 *         need to force march into adjacent open space, that space will be ours
 *         and also the adjacent ones to that space.
 */
public class war {

	public static final int CONFEDERACY = -1;
	public static final int UNION = 1;
	
	protected static Hashtable<String, Integer> costMap = new Hashtable<String, Integer>();
	protected static TreeMap<String, Integer> initialConfig = new TreeMap<String, Integer>();
	protected static TreeMap<String, Integer> currentConfig = new TreeMap<String, Integer>();
	protected static Hashtable<String, ArrayList<String>> allEdges = new Hashtable<String, ArrayList<String>>();
	protected static String outputFile = null;
	protected static String outputLog = null;
	public static int CUTOFF = 3; 
	public static void main(String[] args) {

		String initConfig = null;
		String inputFile = null;

		int taskNumber = 0;
		
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-t")) {
				taskNumber = Integer.parseInt(args[i + 1]);
			}
			if (args[i].equals("-d")) {
				CUTOFF = Integer.parseInt(args[i + 1]);
			}
			if (args[i].equals("-i")) {
				initConfig = args[i + 1];
			}
			if (args[i].equals("-m")) {
				inputFile = args[i + 1];
			}
			if (args[i].equals("-op")) {
				outputFile = args[i + 1];
			} else if (args[i].equals("-ol")) {
				outputLog = args[i + 1];
			}
		}

		String str = null;
		String[] aStr = null;

		Scanner s = null;
		FileReader fr = null;
		try {
			fr = new FileReader(inputFile);
			s = new Scanner(fr);
			while (s.hasNextLine()) {
				str = s.nextLine();
				aStr = str.split(",");
				ArrayList<String> nbrs = allEdges.get(aStr[0]);
				if (nbrs == null) {
					nbrs = new ArrayList<String>();
				}
				nbrs.add(aStr[1]);
				allEdges.put(aStr[0], nbrs);
				nbrs = allEdges.get(aStr[1]);
				if (nbrs == null) {
					nbrs = new ArrayList<String>();
				}
				nbrs.add(aStr[0]);
				allEdges.put(aStr[1], nbrs);
			}
			fr = new FileReader(initConfig);
			s = new Scanner(fr);
			while (s.hasNextLine()) {
				str = s.nextLine();
				aStr = str.split(",");
				costMap.put(aStr[0], Integer.parseInt(aStr[1]));
				initialConfig.put(aStr[0], Integer.parseInt(aStr[2]));
				currentConfig.put(aStr[0], Integer.parseInt(aStr[2]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			s.close();
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (taskNumber == 1) {
			greedyAlgo();
		} else if (taskNumber == 2) {
			minimaxAlgo();
		} else if (taskNumber == 3) {
			alphaBetaAlgo();
		}
	}

	private static void resetConfig() {
		for (Entry<String, Integer> e : initialConfig.entrySet()) {
			currentConfig.put(e.getKey(), e.getValue());
		}

	}

	private static void copyTo(TreeMap<String, Integer> localConfig) {
		for (Entry<String, Integer> e : currentConfig.entrySet()) {
			localConfig.put(e.getKey(), e.getValue());
		}
	}

	private static int getForceMarchVal(String city, int player) {

		TreeMap<String, Integer> localConfig = new TreeMap<String, Integer>();
		copyTo(localConfig);
		ArrayList<String> nbrs = allEdges.get(city);
		String fromCity = null;
		LinkedList<String> q = new LinkedList<String>();
		ArrayList<String> visited = new ArrayList<String>();
		// any nbr has our city occupied?
		for (String nbr : nbrs) {
			if (localConfig.get(nbr) == player) {
				fromCity = nbr;
				break;
			}
		}

		if (fromCity != null) {
			localConfig.put(city, player);
			q.add(city);
			visited.add(city);

			nbrs = allEdges.get(city);
			for (String nbr : nbrs) {
				if (localConfig.get(nbr) == player * -1) {
					localConfig.put(nbr, player);
				}
			}
			return eval(localConfig, player);
		} else {
				return -Integer.MAX_VALUE;
		}
	}

	private static boolean doForceMarch(TreeMap<String, Integer> config,
			String source, int player) {
		
		ArrayList<String> nbrs = allEdges.get(source);
		config.put(source, player);
		boolean isForceMarchPossible = false;
		for (String nbr : nbrs) {
			if (config.get(nbr) == -1 * player) {
				// occupy only when the city is on the other side
				config.put(nbr, player);
				isForceMarchPossible = true;
			}
		}
		
		return isForceMarchPossible;
	}
	
	private static boolean isGameEnd(TreeMap<String, Integer> config){
		for(Integer i : config.values()){
			if(i == 0){
				return false;
			}
		}
		return true;
	}
	private static String getCities(int player){
		StringBuilder res = new StringBuilder("{");
		SortedSet<String> sortedSet = new TreeSet<String>();
		for(Entry<String, Integer> e : currentConfig.entrySet()){
			sortedSet.add(e.getKey());
		}
		for(String s : sortedSet){
			if(currentConfig.get(s) == player){
				res.append(s).append(","); 
			}	
		}
		res.setCharAt(res.length()-1, '}');
		return res.toString();
	}
	private static String getPlayerName(int player){
		return player > 0 ? "Union" : "Confederacy";
	}
	public static void greedyAlgo() {
		resetConfig();
		int player = 1;
		int max = 0;
		int val = 0;
		int turn = 1;
		String action = "N/A";
		String nextCity = null;
		
		System.out.println("TURN = " + 0);
		System.out.println("Player = N/A");
		System.out.println("Action = " + action);
		System.out.println("Destination = N/A");
		System.out.println("Union, " + getCities(UNION) +","+ getStrength(initialConfig, UNION));
		System.out.println("Confederacy, " + getCities(CONFEDERACY) +","+ getStrength(initialConfig, CONFEDERACY));
		System.out.println("----------------------------------------------");
		while (!isGameEnd(currentConfig)) { //TODO: tie-breaking rules
			max = -Integer.MAX_VALUE;
			nextCity = null;
			String city = null;
			boolean isFM = false;
			int oldVal = 0;
			for (Entry<String, Integer> e : currentConfig.entrySet()) {
				if (e.getValue() == 0) {
					city = e.getKey();
					val = getForceMarchVal(city, player);
					if (max < val) {
						max = val;
						isFM = true;
						nextCity = city;
					}
					
					oldVal = currentConfig.get(city);
					currentConfig.put(city, player);
					val = eval(currentConfig, player);
					currentConfig.put(city, oldVal);
					if (max < val) {
						max = val;
						isFM = false;
						nextCity = city;
					}
					
				} 

			}
			if (!isFM) {
				currentConfig.put(nextCity, player);
				action = "Paratroop Drop";
			} else {
				doForceMarch(currentConfig, nextCity, player);
				action = "Force March";
			}
			
			System.out.println("TURN = " + turn);
			System.out.println("Player = " + getPlayerName(player));
			System.out.println("Action = " + action);
			System.out.println("Destination = "+ nextCity);
			System.out.println("Union, " + getCities(1) +","+ getStrength(currentConfig, UNION));
			System.out.println("Confederacy, " + getCities(-1) +","+ getStrength(currentConfig, CONFEDERACY));
			System.out.println("----------------------------------------------");
			player *= -1;
			turn++;
		}
	}
	
	private static double getStrength(TreeMap<String, Integer> config, int player){
		double val = 0.0;
		for(Entry<String, Integer> s : config.entrySet()){
				if(s.getValue() == player){
					val += costMap.get(s.getKey());
				}
		}
		
		return val;
	}

	private static class Node {
		TreeMap<String, Integer> config;
		int minimax_value  = 0;
		int turn = 0;
				
		public Node(TreeMap<String, Integer> config, int minimax_value, int turn) {
			this.config = config;
			this.minimax_value = minimax_value;
			this.turn = turn;
		}
	}
	
	public static void minimaxAlgo() {
		MINIMAX_DECISION();
	}

	public static ArrayList<Node> getAllNextNodes(Node currentNode){

		ArrayList<Node> allNextNodes = new ArrayList<Node>();
		String currentCity = null;
		int currentVal = 0;
		TreeMap<String, Integer> localConfig = null;
		boolean isForceMarchPossible = false;
		Node node = null;
		for (Entry<String, Integer> e : currentConfig.entrySet()) {
			if (e.getValue() == 0) {
				
				currentCity = e.getKey();
				localConfig = new TreeMap<String, Integer>();
				copyTo(localConfig);
				isForceMarchPossible = doForceMarch(localConfig, currentCity, currentNode.turn);
				if(isForceMarchPossible){
					currentVal = getForceMarchVal(currentCity, currentNode.turn);
					node = new Node(localConfig, currentVal, currentNode.turn); //TODO: currentNode.turn might be wrong
					allNextNodes.add(node);
				}
				//Paratroop drop cases;
				localConfig = new TreeMap<String, Integer>();
				copyTo(localConfig);
				localConfig.put(currentCity, currentNode.turn);
				currentVal = eval(currentConfig, currentNode.turn);
				node = new Node(localConfig, currentVal, currentNode.turn); //TODO: currentNode.turn might be wrong
				allNextNodes.add(node);
			} 

		}
		
		return allNextNodes;
	}

	
	public static int currentDepth = 1;

	public static Node MINIMAX_DECISION(){
		
		Node nextMove = null;
		int maxUtility = -1; 
		int currentUtility = 0;
		int player = 1;
		String action = "N/A";
		
		Node startState = new Node(initialConfig, 0, player);
		System.out.println("TURN = " + 0);
		System.out.println("Player = N/A");
		System.out.println("Action = " + action);
		System.out.println("Destination = N/A");
		System.out.println("Union, " + getCities(UNION) +","+ getStrength(initialConfig, UNION));
		System.out.println("Confederacy, " + getCities(CONFEDERACY) +","+ getStrength(initialConfig, CONFEDERACY));
		System.out.println("----------------------------------------------");
		
		for(Node op : getAllNextNodes(startState)){
			currentUtility = MINIMAX_VALUE(op, 1);
			if(maxUtility < currentUtility){
				nextMove = op;
				maxUtility = currentUtility;
			}
		}
		return nextMove;
	}
	private static boolean isTerminal(Node state){
		return isGameEnd(state.config);
	}
	public static int MINIMAX_VALUE(Node state, int player){
		
		int currentVal = 0;
		if(isTerminal(state) || currentDepth == CUTOFF){
			return eval(state.config, player);
		}else if(player == 1){
			int max = -1;
			for(Node successor : getAllNextNodes(state)){
				currentVal = MINIMAX_VALUE(successor, -1*player);
				if(max < currentVal){
					max = currentVal;
				}
			}
			currentDepth++;
			return max;
		}else{
			int min = -1;
			for(Node successor : getAllNextNodes(state)){
				currentVal = MINIMAX_VALUE(successor, -1*player);
				if(min > currentVal){
					min = currentVal;
				}
			}
			currentDepth++;
			return min;
		}
	}
	public static void alphaBetaAlgo() {

	}

	private static int eval(TreeMap<String, Integer> config, int player) {

		int val1 = 0;
		int val2 = 0;
		for (Entry<String, Integer> e : config.entrySet()) {
			if (e.getValue() == player) {
				val1 += costMap.get(e.getKey());
			}else if(e.getValue() == -1*player){
				val2 += costMap.get(e.getKey());
			}
		}

		return val1-val2;
	}
}
