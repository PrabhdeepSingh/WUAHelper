package com.xplosivegames.wuahelper;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.logging.Level;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.webinterface.WebInterface;
import com.wurmonline.server.webinterface.WebInterfaceImpl;

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
				case "ban":
					System.out.println(banPlayer(paramters[0].toString(), paramters[1].toString(), paramters[2].toString(), Integer.parseInt(paramters[3])));
					break;
				case "pardon":
					System.out.println(pardonPlayer(paramters[0].toString(), paramters[1].toString()));
					break;
				case "mutePlayer":
					System.out.println(mutePlayer(paramters[0].toString(), paramters[1].toString(), Integer.parseInt(paramters[2])));
					break;
				case "unMutePlayer":
					System.out.println(unmutePlayer(paramters[0].toString()));
					break;
				case "addMoney":
					System.out.println(addMoney(paramters[0].toString(), Long.parseLong((paramters[1]))));
					break;
				case "changePower":
					System.out.println(changePower(paramters[0].toString(), Integer.parseInt((paramters[1]))));
					break;
				case "addItem":
					System.out.println(addItem(paramters[0].toString(), Integer.parseInt((paramters[1])), Float.parseFloat((paramters[2])), Byte.parseByte((paramters[3])), paramters[4].toString(), Integer.parseInt((paramters[5]))));
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

	/**
	 * Gets current wurm time
	 * @return
	 * @throws RemoteException
	 */
	private static String getWurmTime() throws RemoteException {
		return wurm.getWurmTime();
	}
	
	/**
	 * Starts a server shutdown
	 * @param user
	 * @param seconds
	 * @param reason
	 * @return
	 */
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
	
	/**
	 * Checks if the server / RMI is still running
	 * @return
	 * @throws RemoteException
	 */
	private static boolean isRunning() throws RemoteException {
		return wurm.isRunning();
	}
	
	/**
	 * Bans a player (Doesn't kick them from the server)
	 * @param playerName
	 * @param ip
	 * @param reason
	 * @param days
	 * @return Boolean
	 * @throws RemoteException 
	 * @throws NoSuchPlayerException 
	 */
	private static boolean banPlayer(String playerName, String ip, String reason, int days) {
		try {
			if(wurm.wuaBan(playerName, ip, reason, days)) {
				return true;
			}
			else {
				return false;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
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
	 * Mutes a player from taking in-game
	 * @param playerName
	 * @param reason
	 * @param hours
	 * @return Boolean
	 */
	private static boolean mutePlayer(String playerName, String reason, int hours) {
		
		try {
			if(wurm.wuaMute(playerName, reason, hours)) {
				return true;
			}
			else {
				return false;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Gives a player his/her voice back 
	 * @param playerName
	 * @return
	 */
	private static boolean unmutePlayer(String playerName) {
		
		try {
			if(wurm.wuaUnMute(playerName)) {
				return true;
			}
			else {
				return false;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
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
	
	private static boolean changePower(String playerName, int power) {
		try {
			if(wurm.wuaChangePower(playerName, power)) {
				return true;
			}
			else {
				return false;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean addItem(String playerName, int itemTemplateID, float itemQuality, byte itemRarity, String creator, int itemAmount) {
		try {
			if(wurm.wuaGiveItem(playerName, itemTemplateID, itemQuality, itemRarity, creator, itemAmount)) {
				return true;
			}
			else {
				return false;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
}
