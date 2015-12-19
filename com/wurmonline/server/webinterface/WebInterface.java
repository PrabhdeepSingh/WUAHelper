package com.wurmonline.server.webinterface;

import com.wurmonline.server.players.BannedIp;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface WebInterface extends Remote {
   int DEFAULT_RMI_PORT = 7220;
   int DEFAULT_REGISTRATION_PORT = 7221;

   int getPower(String var1, long var2) throws RemoteException;

   boolean isRunning(String var1) throws RemoteException;

   int getPlayerCount(String var1) throws RemoteException;

   int getPremiumPlayerCount(String var1) throws RemoteException;

   String getTestMessage(String var1) throws RemoteException;

   void broadcastMessage(String var1, String var2) throws RemoteException;

   long getAccountStatusForPlayer(String var1, String var2) throws RemoteException;

   long chargeMoney(String var1, String var2, long var3) throws RemoteException;

   String getServerStatus(String var1) throws RemoteException;

   Map getBattleRanks(String var1, int var2) throws RemoteException;

   Map getFriends(String var1, long var2) throws RemoteException;

   Map getInventory(String var1, long var2) throws RemoteException;

   Map getBodyItems(String var1, long var2) throws RemoteException;

   Map getSkills(String var1, long var2) throws RemoteException;

   Map getPlayerSummary(String var1, long var2) throws RemoteException;

   long getLocalCreationTime(String var1) throws RemoteException;

   String ban(String var1, String var2, String var3, int var4) throws RemoteException;

   String pardonban(String var1, String var2) throws RemoteException;

   String addBannedIp(String var1, String var2, String var3, int var4) throws RemoteException;

   BannedIp[] getPlayersBanned(String var1) throws RemoteException;

   BannedIp[] getIpsBanned(String var1) throws RemoteException;

   String removeBannedIp(String var1, String var2) throws RemoteException;

   Map getKingdoms(String var1) throws RemoteException;

   Map getPlayersForKingdom(String var1, int var2) throws RemoteException;

   long getPlayerId(String var1, String var2) throws RemoteException;

   Map createPlayer(String var1, String var2, String var3, String var4, String var5, String var6, byte var7, byte var8, long var9, byte var11) throws RemoteException;

   Map createPlayerPhaseOne(String var1, String var2, String var3) throws RemoteException;

   Map createPlayerPhaseTwo(String var1, String var2, String var3, String var4, String var5, String var6, byte var7, byte var8, long var9, byte var11, String var12) throws RemoteException;

   Map createPlayerPhaseTwo(String var1, String var2, String var3, String var4, String var5, String var6, byte var7, byte var8, long var9, byte var11, String var12, int var13) throws RemoteException;

   Map createPlayerPhaseTwo(String var1, String var2, String var3, String var4, String var5, String var6, byte var7, byte var8, long var9, byte var11, String var12, int var13, boolean var14) throws RemoteException;

   byte[] createAndReturnPlayer(String var1, String var2, String var3, String var4, String var5, String var6, byte var7, byte var8, long var9, byte var11, boolean var12, boolean var13, boolean var14) throws RemoteException;

   Map addMoneyToBank(String var1, String var2, long var3, String var5) throws RemoteException;

   long getMoney(String var1, long var2, String var4) throws RemoteException;

   Map reversePayment(String var1, long var2, int var4, int var5, String var6, String var7, String var8) throws RemoteException;

   Map addMoneyToBank(String var1, String var2, long var3, String var5, boolean var6) throws RemoteException;

   Map addMoneyToBank(String var1, String var2, long var3, long var5, String var7, boolean var8) throws RemoteException;

   Map addPlayingTime(String var1, String var2, int var3, int var4, String var5, boolean var6) throws RemoteException;

   Map addPlayingTime(String var1, String var2, int var3, int var4, String var5) throws RemoteException;

   Map getDeeds(String var1) throws RemoteException;

   Map getDeedSummary(String var1, int var2) throws RemoteException;

   Map getPlayersForDeed(String var1, int var2) throws RemoteException;

   Map getAlliesForDeed(String var1, int var2) throws RemoteException;

   String[] getHistoryForDeed(String var1, int var2, int var3) throws RemoteException;

   String[] getAreaHistory(String var1, int var2) throws RemoteException;

   Map getItemSummary(String var1, long var2) throws RemoteException;

   Map getPlayerIPAddresses(String var1) throws RemoteException;

   Map getNameBans(String var1) throws RemoteException;

   Map getIPBans(String var1) throws RemoteException;

   Map getWarnings(String var1) throws RemoteException;

   String getWurmTime(String var1) throws RemoteException;

   String getUptime(String var1) throws RemoteException;

   String getNews(String var1) throws RemoteException;

   String getGameInfo(String var1) throws RemoteException;

   Map getKingdomInfluence(String var1) throws RemoteException;

   Map getMerchantSummary(String var1, long var2) throws RemoteException;

   Map getBankAccount(String var1, long var2) throws RemoteException;

   Map authenticateUser(String var1, String var2, String var3, String var4, Map var5) throws RemoteException;

   Map authenticateUser(String var1, String var2, String var3, String var4) throws RemoteException;

   Map changePassword(String var1, String var2, String var3, String var4) throws RemoteException;

   Map changePassword(String var1, String var2, String var3, String var4, String var5) throws RemoteException;

   boolean changePassword(String var1, long var2, String var4) throws RemoteException;

   Map changeEmail(String var1, String var2, String var3, String var4) throws RemoteException;

   String getChallengePhrase(String var1, String var2) throws RemoteException;

   String[] getPlayerNamesForEmail(String var1, String var2) throws RemoteException;

   String getEmailAddress(String var1, String var2) throws RemoteException;

   Map requestPasswordReset(String var1, String var2, String var3) throws RemoteException;

   Map getAllServers(String var1) throws RemoteException;

   Map getAllServerInternalAddresses(String var1) throws RemoteException;

   boolean sendMail(String var1, String var2, String var3, String var4, String var5) throws RemoteException;

   Map getPendingAccounts(String var1) throws RemoteException;

   void shutDown(String var1, String var2, String var3, String var4, int var5) throws RemoteException;

   Map getReferrers(String var1, long var2) throws RemoteException;

   String addReferrer(String var1, String var2, long var3) throws RemoteException;

   String acceptReferrer(String var1, long var2, String var4, boolean var5) throws RemoteException;

   Map getSkillStats(String var1, int var2) throws RemoteException;

   Map getSkills(String var1) throws RemoteException;

   Map getStructureSummary(String var1, long var2) throws RemoteException;

   long getStructureIdFromWrit(String var1, long var2) throws RemoteException;

   Map getTileSummary(String var1, int var2, int var3, boolean var4) throws RemoteException;

   String getReimbursementInfo(String var1, String var2) throws RemoteException;

   boolean withDraw(String var1, String var2, String var3, String var4, int var5, int var6, boolean var7, int var8) throws RemoteException;

   boolean transferPlayer(String var1, String var2, int var3, int var4, boolean var5, int var6, byte[] var7) throws RemoteException;

   boolean setCurrentServer(String var1, String var2, int var3) throws RemoteException;

   boolean addDraggedItem(String var1, long var2, byte[] var4, long var5, int var7, int var8) throws RemoteException;

   String rename(String var1, String var2, String var3, String var4, int var5) throws RemoteException;

   String changePassword(String var1, String var2, String var3, String var4, int var5) throws RemoteException;

   String changeEmail(String var1, String var2, String var3, String var4, String var5, int var6, String var7, String var8) throws RemoteException;

   String addReimb(String var1, String var2, String var3, int var4, int var5, int var6, boolean var7) throws RemoteException;

   long[] getCurrentServerAndWurmid(String var1, String var2, long var3) throws RemoteException;

   Map getPlayerStates(String var1, long[] var2) throws RemoteException, WurmServerException;

   void manageFeature(String var1, int var2, int var3, boolean var4, boolean var5, boolean var6) throws RemoteException;

   void startShutdown(String var1, String var2, int var3, String var4) throws RemoteException;

   String sendMail(String var1, byte[] var2, byte[] var3, long var4, long var6, int var8) throws RemoteException;

   String setPlayerPremiumTime(String var1, long var2, long var4, int var6, int var7, String var8) throws RemoteException;

   String setPlayerMoney(String var1, long var2, long var4, long var6, String var8) throws RemoteException;

   Map doesPlayerExist(String var1, String var2) throws RemoteException;

   void setWeather(String var1, float var2, float var3, float var4) throws RemoteException;

   String sendVehicle(String var1, byte[] var2, byte[] var3, long var4, long var6, int var8, int var9, int var10, int var11, float var12) throws RemoteException;

   void requestDemigod(String var1, byte var2, String var3) throws RemoteException;

   String ascend(String var1, int var2, String var3, long var4, byte var6, byte var7, byte var8, float var9, float var10) throws RemoteException;

   boolean requestDeityMove(String var1, int var2, int var3, String var4) throws RemoteException;

   void setKingdomInfo(String var1, int var2, byte var3, byte var4, String var5, String var6, String var7, String var8, String var9, String var10, boolean var11) throws RemoteException;

   boolean kingdomExists(String var1, int var2, byte var3, boolean var4) throws RemoteException;

   void genericWebCommand(String var1, short var2, long var3, byte[] var5) throws RemoteException;

   int[] getPremTimeSilvers(String var1, long var2) throws RemoteException;

   void awardPlayer(String var1, long var2, String var4, int var5, int var6) throws RemoteException;
   
   boolean wuaBan(String var1, String var2, String var3, String var4, int var5) throws RemoteException;
   
   boolean wuaMute(String var1, String var2, String var3, int var4) throws RemoteException;
   
   boolean wuaUnMute(String var1, String var2) throws RemoteException;
   
   boolean wuaChangePower(String var1, String var2, final int var3) throws RemoteException;
   
   boolean wuaChangeKingdom(String var1, String var2, final int var3) throws RemoteException;
   
   boolean wuaGiveItem(String var1, String var2, int var3, float var4, byte var5, String var6, int var7) throws RemoteException;
}
