package com.xplosivegames.wuahelper;

import java.rmi.Naming;
import java.rmi.RemoteException;

import com.wurmonline.server.webinterface.WebInterface;

public class WUAhelper {
	
	private static WebInterface wurm;
	
	public static void main(String[] args) {
		String ip = args[0];
		String port = args[1];
		String rmiPassword = args[2];
		String doing = args[3];
		String[] paramters = args[4].split(",", -1);
		String connectionString = "//" + ip + ":" + port + "/" + rmiPassword;
		try {
			wurm = (WebInterface) Naming.lookup(connectionString);
			switch(doing) {
				case "playerCount":
					System.out.println(getPlayerCount());
					break;
				case "ban":
					System.out.println(banPlayer(paramters[0].toString(), paramters[1].toString(), paramters[2].toString(), Integer.parseInt(paramters[3])));
					break;
				case "pardon":
					System.out.println(pardonPlayer(paramters[0].toString(), paramters[1].toString()));
					break;
				case "addMoney":
					System.out.println(addMoney(paramters[0].toString(), Long.parseLong((paramters[1]))));
					break;
				case "broadcast":
					System.out.println(broadcastMessage(paramters[0].toString()));
					break;
				case "uptime":
					System.out.println(getUpTime());
					break;
				case "wurmTime":
					System.out.println(getWurmTime());
					break;
				case "isRunning":
					System.out.println(isRunning());
					break;
				case "shutDown":
					System.out.println(shutDown(paramters[0].toString(), Integer.parseInt(paramters[1].toString()), paramters[2].toString()));
					break;
				default:
					System.out.println("No valid method supplied");
					break;
			}
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Gets a player count on the server
	 * @return Integer Player count
	 * @throws RemoteException
	 */
	private static int getPlayerCount() throws RemoteException {
		return wurm.getPlayerCount();
	}
	
	/**
	 * Bans a player (Doesn't kick them from the server)
	 * @param playerName
	 * @param ip
	 * @param reason
	 * @param days
	 * @return Boolean
	 */
	private static boolean banPlayer(String playerName, String ip, String reason, int days) {
		try {
			wurm.addBannedIp(ip, reason, days);
			wurm.ban(playerName, reason, days);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Unbans / pardons a player and IP
	 * @param playerName
	 * @param ip
	 * @return Boolean
	 */
	private static boolean pardonPlayer(String playerName, String ip) {
		try {
			wurm.removeBannedIp(ip);
			wurm.pardonban(playerName);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Adds money to a players bank account
	 * @param playerName
	 * @param amount
	 * @return Boolean
	 */
	private static boolean addMoney(String playerName, long amount) {
		try {
			wurm.addMoneyToBank(playerName, amount, "");
			return true;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Broadcasts a message through the server
	 * @param message
	 * @return Boolean
	 */
	private static boolean broadcastMessage(String message) {
		try {
			wurm.broadcastMessage(message);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Gets the server uptime
	 * @return
	 * @throws RemoteException
	 */
	private static String getUpTime() throws RemoteException {
		return wurm.getUptime();
	}

	private static String getWurmTime() throws RemoteException {
		return wurm.getWurmTime();
	}
	
	private static boolean shutDown(String user, int seconds, String reason) {
		try {
			wurm.startShutdown(user, seconds, reason);
			return true;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	private static boolean isRunning() throws RemoteException {
		return wurm.isRunning();
	}
}
