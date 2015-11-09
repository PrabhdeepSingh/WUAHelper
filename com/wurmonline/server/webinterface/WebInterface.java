// 
// Decompiled by Procyon v0.5.30
// 

package com.wurmonline.server.webinterface;

import com.wurmonline.shared.exceptions.WurmServerException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.players.BannedIp;
import java.util.Map;
import java.rmi.RemoteException;
import java.rmi.Remote;

public interface WebInterface extends Remote
{
    public static final int DEFAULT_RMI_PORT = 7220;
    public static final int DEFAULT_REGISTRATION_PORT = 7221;
    
    int getPower(final long p0) throws RemoteException;
    
    boolean isRunning() throws RemoteException;
    
    int getPlayerCount() throws RemoteException;
    
    int getPremiumPlayerCount() throws RemoteException;
    
    String getTestMessage() throws RemoteException;
    
    void broadcastMessage(final String p0) throws RemoteException;
    
    long getAccountStatusForPlayer(final String p0) throws RemoteException;
    
    long chargeMoney(final String p0, final long p1) throws RemoteException;
    
    String getServerStatus() throws RemoteException;
    
    Map<String, Integer> getBattleRanks(final int p0) throws RemoteException;
    
    Map<String, Long> getFriends(final long p0) throws RemoteException;
    
    Map<String, String> getInventory(final long p0) throws RemoteException;
    
    Map<Long, Long> getBodyItems(final long p0) throws RemoteException;
    
    Map<String, Float> getSkills(final long p0) throws RemoteException;
    
    Map<String, ?> getPlayerSummary(final long p0) throws RemoteException;
    
    long getLocalCreationTime() throws RemoteException;
    
    String ban(final String p0, final String p1, final int p2) throws RemoteException;
    
    String pardonban(final String p0) throws RemoteException;
    
    String addBannedIp(final String p0, final String p1, final int p2) throws RemoteException;
    
    BannedIp[] getPlayersBanned() throws RemoteException;
    
    BannedIp[] getIpsBanned() throws RemoteException;
    
    String removeBannedIp(final String p0) throws RemoteException;
    
    Map<Integer, String> getKingdoms() throws RemoteException;
    
    Map<Long, String> getPlayersForKingdom(final int p0) throws RemoteException;
    
    long getPlayerId(final String p0) throws RemoteException;
    
    Map<String, ?> createPlayer(final String p0, final String p1, final String p2, final String p3, final String p4, final byte p5, final byte p6, final long p7, final byte p8) throws RemoteException;
    
    Map<String, String> createPlayerPhaseOne(final String p0, final String p1) throws RemoteException;
    
    Map<String, ?> createPlayerPhaseTwo(final String p0, final String p1, final String p2, final String p3, final String p4, final byte p5, final byte p6, final long p7, final byte p8, final String p9) throws RemoteException;
    
    Map<String, ?> createPlayerPhaseTwo(final String p0, final String p1, final String p2, final String p3, final String p4, final byte p5, final byte p6, final long p7, final byte p8, final String p9, final int p10) throws RemoteException;
    
    Map<String, ?> createPlayerPhaseTwo(final String p0, final String p1, final String p2, final String p3, final String p4, final byte p5, final byte p6, final long p7, final byte p8, final String p9, final int p10, final boolean p11) throws RemoteException;
    
    byte[] createAndReturnPlayer(final String p0, final String p1, final String p2, final String p3, final String p4, final byte p5, final byte p6, final long p7, final byte p8, final boolean p9, final boolean p10, final boolean p11) throws RemoteException;
    
    Map<String, String> addMoneyToBank(final String p0, final long p1, final String p2) throws RemoteException;
    
    long getMoney(final long p0, final String p1) throws RemoteException;
    
    Map<String, String> reversePayment(final long p0, final int p1, final int p2, final String p3, final String p4, final String p5) throws RemoteException;
    
    Map<String, String> addMoneyToBank(final String p0, final long p1, final String p2, final boolean p3) throws RemoteException;
    
    Map<String, String> addMoneyToBank(final String p0, final long p1, final long p2, final String p3, final boolean p4) throws RemoteException;
    
    Map<String, String> addPlayingTime(final String p0, final int p1, final int p2, final String p3, final boolean p4) throws RemoteException;
    
    Map<String, String> addPlayingTime(final String p0, final int p1, final int p2, final String p3) throws RemoteException;
    
    Map<Integer, String> getDeeds() throws RemoteException;
    
    Map<String, ?> getDeedSummary(final int p0) throws RemoteException;
    
    Map<String, Long> getPlayersForDeed(final int p0) throws RemoteException;
    
    Map<String, Integer> getAlliesForDeed(final int p0) throws RemoteException;
    
    String[] getHistoryForDeed(final int p0, final int p1) throws RemoteException;
    
    String[] getAreaHistory(final int p0) throws RemoteException;
    
    Map<String, ?> getItemSummary(final long p0) throws RemoteException;
    
    Map<String, String> getPlayerIPAddresses() throws RemoteException;
    
    Map<String, String> getNameBans() throws RemoteException;
    
    Map<String, String> getIPBans() throws RemoteException;
    
    Map<String, String> getWarnings() throws RemoteException;
    
    String getWurmTime() throws RemoteException;
    
    String getUptime() throws RemoteException;
    
    String getNews() throws RemoteException;
    
    String getGameInfo() throws RemoteException;
    
    Map<String, String> getKingdomInfluence() throws RemoteException;
    
    Map<String, ?> getMerchantSummary(final long p0) throws RemoteException;
    
    Map<String, ?> getBankAccount(final long p0) throws RemoteException;
    
    Map<String, ?> authenticateUser(final String p0, final String p1, final String p2, final Map p3) throws RemoteException;
    
    Map<String, ?> authenticateUser(final String p0, final String p1, final String p2) throws RemoteException;
    
    Map<String, String> changePassword(final String p0, final String p1, final String p2) throws RemoteException;
    
    Map<String, String> changePassword(final String p0, final String p1, final String p2, final String p3) throws RemoteException;
    
    boolean changePassword(final long p0, final String p1) throws RemoteException;
    
    Map<String, String> changeEmail(final String p0, final String p1, final String p2) throws RemoteException;
    
    String getChallengePhrase(final String p0) throws RemoteException;
    
    String[] getPlayerNamesForEmail(final String p0) throws RemoteException;
    
    String getEmailAddress(final String p0) throws RemoteException;
    
    Map<String, String> requestPasswordReset(final String p0, final String p1) throws RemoteException;
    
    Map<Integer, String> getAllServers() throws RemoteException;
    
    Map<Integer, String> getAllServerInternalAddresses() throws RemoteException;
    
    boolean sendMail(final String p0, final String p1, final String p2, final String p3) throws RemoteException;
    
    Map<String, String> getPendingAccounts() throws RemoteException;
    
    void shutDown(final String p0, final String p1, final String p2, final int p3) throws RemoteException;
    
    Map<String, Byte> getReferrers(final long p0) throws RemoteException;
    
    String addReferrer(final String p0, final long p1) throws RemoteException;
    
    String acceptReferrer(final long p0, final String p1, final boolean p2) throws RemoteException;
    
    Map<String, Double> getSkillStats(final int p0) throws RemoteException;
    
    Map<Integer, String> getSkills() throws RemoteException;
    
    Map<String, ?> getStructureSummary(final long p0) throws RemoteException;
    
    long getStructureIdFromWrit(final long p0) throws RemoteException;
    
    Map<String, ?> getTileSummary(final int p0, final int p1, final boolean p2) throws RemoteException;
    
    String getReimbursementInfo(final String p0) throws RemoteException;
    
    boolean withDraw(final String p0, final String p1, final String p2, final int p3, final int p4, final boolean p5, final int p6) throws RemoteException;
    
    boolean transferPlayer(final String p0, final int p1, final int p2, final boolean p3, final int p4, final byte[] p5) throws RemoteException;
    
    boolean setCurrentServer(final String p0, final int p1) throws RemoteException;
    
    boolean addDraggedItem(final long p0, final byte[] p1, final long p2, final int p3, final int p4) throws RemoteException;
    
    String rename(final String p0, final String p1, final String p2, final int p3) throws RemoteException;
    
    String changePassword(final String p0, final String p1, final String p2, final int p3) throws RemoteException;
    
    String changeEmail(final String p0, final String p1, final String p2, final String p3, final int p4, final String p5, final String p6) throws RemoteException;
    
    String addReimb(final String p0, final String p1, final int p2, final int p3, final int p4, final boolean p5) throws RemoteException;
    
    long[] getCurrentServerAndWurmid(final String p0, final long p1) throws RemoteException;
    
    Map<Long, byte[]> getPlayerStates(final long[] p0) throws RemoteException, WurmServerException;
    
    void manageFeature(final int p0, final int p1, final boolean p2, final boolean p3, final boolean p4) throws RemoteException;
    
    void startShutdown(final String p0, final int p1, final String p2) throws RemoteException;
    
    String sendMail(final byte[] p0, final byte[] p1, final long p2, final long p3, final int p4) throws RemoteException;
    
    String setPlayerPremiumTime(final long p0, final long p1, final int p2, final int p3, final String p4) throws RemoteException;
    
    String setPlayerMoney(final long p0, final long p1, final long p2, final String p3) throws RemoteException;
    
    Map<String, String> doesPlayerExist(final String p0) throws RemoteException;
    
    void setWeather(final float p0, final float p1, final float p2) throws RemoteException;
    
    String sendVehicle(final byte[] p0, final byte[] p1, final long p2, final long p3, final int p4, final int p5, final int p6, final int p7, final float p8) throws RemoteException;
    
    void requestDemigod(final byte p0, final String p1) throws RemoteException;
    
    String ascend(final int p0, final String p1, final long p2, final byte p3, final byte p4, final byte p5, final float p6, final float p7) throws RemoteException;
    
    boolean requestDeityMove(final int p0, final int p1, final String p2) throws RemoteException;
    
    void setKingdomInfo(final int p0, final byte p1, final byte p2, final String p3, final String p4, final String p5, final String p6, final String p7, final String p8, final boolean p9) throws RemoteException;
    
    boolean kingdomExists(final int p0, final byte p1, final boolean p2) throws RemoteException;
    
    void genericWebCommand(final short p0, final long p1, final byte[] p2) throws RemoteException;
    
    int[] getPremTimeSilvers(final long p0) throws RemoteException;
    
    void awardPlayer(final long p0, final String p1, final int p2, final int p3) throws RemoteException;
    
    boolean wuaBan(final String p0, final String p1, final String p2, final int p3) throws RemoteException;
    
    boolean wuaMute(final String p0, final String p1, final int p2) throws RemoteException;
    
    boolean wuaUnMute(final String p0) throws RemoteException;
    
    boolean wuaChangePower(final String p0, final int p1) throws RemoteException;
    
    boolean wuaChangeKingdom(final String p0, final int p1) throws RemoteException;
    
    boolean wuaGiveItem(final String p0, final int p1, final float p2, final byte p3, final String p4, final int p5) throws RemoteException;
}
