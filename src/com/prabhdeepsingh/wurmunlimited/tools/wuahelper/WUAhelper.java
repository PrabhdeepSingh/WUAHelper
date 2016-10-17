package com.prabhdeepsingh.wurmunlimited.tools.wuahelper;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.webinterface.WebInterface;

public class WUAhelper {
	
	public static void main(String[] args) {
		String ip = args[0];
		String port = args[1];
		String rmiPassword = args[2];
		String doing = args[3];
		String[] paramters = args[4].split(",", -1);
		
		try {
			WebInterface wuInterface = initRMI(ip, Integer.parseInt(port));
			switch(doing) {
				case "playerCount":
					System.out.println(getPlayerCount(wuInterface, rmiPassword));
					break;
				case "broadcast":
					System.out.println(broadcastMessage(wuInterface, rmiPassword, paramters[0].toString()));
					break;
				case "uptime":
					System.out.println(getUpTime(wuInterface, rmiPassword));
					break;
				case "wurmTime":
					System.out.println(getWurmTime(wuInterface, rmiPassword));
					break;
				case "isRunning":
					System.out.println(isRunning(wuInterface, rmiPassword));
					break;
				case "shutDown":
					System.out.println(shutDown(wuInterface, rmiPassword, paramters[0].toString(), Integer.parseInt(paramters[1].toString()), paramters[2].toString()));
					break;
				case "ban":
					System.out.println(banPlayer(wuInterface, rmiPassword, paramters[0].toString(), paramters[1].toString(), paramters[2].toString(), Integer.parseInt(paramters[3])));
					break;
				case "pardon":
					System.out.println(pardonPlayer(wuInterface, rmiPassword, paramters[0].toString(), paramters[1].toString()));
					break;
				case "mutePlayer":
					System.out.println(mutePlayer(wuInterface, rmiPassword, paramters[0].toString(), paramters[1].toString(), Integer.parseInt(paramters[2])));
					break;
				case "unMutePlayer":
					System.out.println(unmutePlayer(wuInterface, rmiPassword, paramters[0].toString()));
					break;
				case "addMoney":
					System.out.println(addMoney(wuInterface, rmiPassword, paramters[0].toString(), Long.parseLong((paramters[1]))));
					break;
				case "changePower":
					System.out.println(changePower(wuInterface, rmiPassword, paramters[0].toString(), Integer.parseInt((paramters[1]))));
					break;
				case "addItem":
					System.out.println(addItem(wuInterface, rmiPassword, paramters[0].toString(), Integer.parseInt((paramters[1])), Float.parseFloat((paramters[2])), Byte.parseByte((paramters[3])), paramters[4].toString(), Integer.parseInt((paramters[5]))));
					break;
				case "changeKingdom":
					System.out.println(changeKingdom(wuInterface, rmiPassword, paramters[0].toString(), Integer.parseInt(paramters[1])));
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

	
	private static WebInterface initRMI(String ip, int port) throws RemoteException, NotBoundException
	{
		Registry registry = LocateRegistry.getRegistry(ip, port);
		return (WebInterface) registry.lookup("wuinterface");
	}
	
	/**
	 * Gets a player count on the server
	 * @return Integer Player count
	 * @throws RemoteException
	 */
	private static int getPlayerCount(WebInterface wuInterface, String rmiPassword) throws RemoteException {
		return wuInterface.getPlayerCount(rmiPassword);
	}
	
	/**
	 * Broadcasts a message through the server
	 * @param message
	 * @return Boolean
	 */
	private static boolean broadcastMessage(WebInterface wuInterface, String rmiPassword, String message) {
		try {
			wuInterface.broadcastMessage(rmiPassword, message);
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
	private static String getUpTime(WebInterface wuInterface, String rmiPassword) throws RemoteException {
		return wuInterface.getUptime(rmiPassword);
	}

	/**
	 * Gets current wurm time
	 * @return
	 * @throws RemoteException
	 */
	private static String getWurmTime(WebInterface wuInterface, String rmiPassword) throws RemoteException {
		return wuInterface.getWurmTime(rmiPassword);
	}
	
	/**
	 * Starts a server shutdown
	 * @param user
	 * @param seconds
	 * @param reason
	 * @return
	 */
	private static boolean shutDown(WebInterface wuInterface, String rmiPassword, String user, int seconds, String reason) {
		try {
			wuInterface.startShutdown(rmiPassword, user, seconds, reason);
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
	private static boolean isRunning(WebInterface wuInterface, String rmiPassword) throws RemoteException {
		return wuInterface.isRunning(rmiPassword);
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
	private static boolean banPlayer(WebInterface wuInterface, String rmiPassword, String playerName, String ip, String reason, int days) {
		try {
			if(wuInterface.wuaBan(rmiPassword, playerName, ip, reason, days)) {
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
	private static boolean pardonPlayer(WebInterface wuInterface, String rmiPassword, String playerName, String ip) {
		try {
			wuInterface.removeBannedIp(rmiPassword, ip);
			wuInterface.pardonban(rmiPassword, playerName);
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
	private static boolean mutePlayer(WebInterface wuInterface, String rmiPassword, String playerName, String reason, int hours) {
		
		try {
			if(wuInterface.wuaMute(rmiPassword, playerName, reason, hours)) {
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
	private static boolean unmutePlayer(WebInterface wuInterface, String rmiPassword, String playerName) {
		
		try {
			if(wuInterface.wuaUnMute(rmiPassword, playerName)) {
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
	private static boolean addMoney(WebInterface wuInterface, String rmiPassword, String playerName, long amount) {
		try {
			wuInterface.addMoneyToBank(rmiPassword, playerName, amount, "");
			return true;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean changePower(WebInterface wuInterface, String rmiPassword, String playerName, int power) {
		try {
			if(wuInterface.wuaChangePower(rmiPassword, playerName, power)) {
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
	
	private static boolean addItem(WebInterface wuInterface, String rmiPassword, String playerName, int itemTemplateID, float itemQuality, byte itemRarity, String creator, int itemAmount) {
		try {
			if(wuInterface.wuaGiveItem(rmiPassword, playerName, itemTemplateID, itemQuality, itemRarity, creator, itemAmount)) {
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
	
	private static boolean changeKingdom(WebInterface wuInterface, String rmiPassword, String playerName, int newKingdom) {
		try {
			if(wuInterface.wuaChangeKingdom(rmiPassword, playerName, newKingdom)) {
				return true;
			}
			else {
				return true;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
	}
}
