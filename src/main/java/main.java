import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.model.shared.Asset;
import com.beimin.eveapi.parser.ApiAuthorization;
import com.beimin.eveapi.parser.corporation.AssetListParser;
import com.beimin.eveapi.response.shared.AssetListResponse;
import com.google.common.io.Files;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import java.io.*;
import java.sql.*;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
//import com.beimin.eveapi.model.corporation.Starbase;
//import com.beimin.eveapi.parser.corporation.StarbaseListParser;
//import com.beimin.eveapi.response.corporation.StarbaseListResponse;

public class main {
    static main obj = new main();
    public static void main(String[] args) {
        try {
            //bot auth key
            String args1 = args[0];
            //Auth keyID
            String args2 = args[1];
            //Auth vCode param
            String args3 = args[2];
            //convert string args to integer
            int args4 = Integer.parseInt(args2);
            String args5 = args[3];

            String filetest = obj.zipDecompress();
            getResponse(args1, args4, args3, args5, filetest);
            try {
                File file = new File(filetest);
                if(file.delete()) {
                    System.out.println(file.getName() + " was deleted successfully.");
                    System.out.println("JAR Exiting...");
                    System.exit(0);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    public static void getResponse(String BotAuthKey, int keyId, String vCode, String file, String User) throws ApiException {
        //Create Slack web socket connection
        SlackSession session = SlackSessionFactory.createWebSocketSlackSession(BotAuthKey);
        try {
            session.connect();
        }catch (IOException e){
            e.printStackTrace();
        }
        SlackChannel channel = session.findChannelByName("random");
        SlackUser user = session.findUserByUserName(User);

        ApiAuthorization auth = new ApiAuthorization(keyId, vCode);
        AssetListParser parser = new AssetListParser();
        AssetListResponse response = parser.getResponse(auth);

        //Collection of all assets
        Collection<Asset> assets = response.getAll();
        for(Asset asset : assets) {
            //look for subasset
            if (asset.getTypeID() == 14343) {
                Collection<Asset> subAssets = asset.getAssets();
                for (Asset subAsset : subAssets) {
                    int quantity = subAsset.getQuantity();
                    long systemid = asset.getLocationID();
                    int systemidint = (int) systemid;
                    String systemname;
                    try {
                        String sql = "SELECT solarSystemName FROM mapSolarSystems WHERE solarSystemID = ? ";
                        systemname = sqlConnect(systemidint, sql, file);
                        if(quantity % 100 != 0 && quantity > 0) {
                            session.sendMessageToUser(user, "Possible siphon detected in " + systemname, null);
                            session.sendMessage(channel, "Possible siphon detected in " + systemname, null);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    /*
    public static int moonList(int keyId, String vCode) throws ApiException {
        ApiAuthorization auth = new ApiAuthorization(keyId, vCode);
        StarbaseListParser parser = new StarbaseListParser();
        StarbaseListResponse response = parser.getResponse(auth);

        Collection<Starbase> starbases = response.getAll();
        for (Starbase starbase : starbases) {
            long itemID = starbase.getItemID();
            int moonID = starbase.getMoonID();
        }
        return 0;
    }
    */
    public String zipDecompress(){
        File folder = Files.createTempDir();
        byte[] buffer = new byte[1024];
        System.out.println(folder);
        InputStream in = getClass().getResourceAsStream("/staticdataexport.zip");
        try {
            ZipInputStream zis = new ZipInputStream(in);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null) {
                String fileName = ze.getName();
                File newFile = new File(folder + File.separator + fileName);
                System.out.println("Unzipped to: " +newFile);
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                return newFile.toString();
            }
            zis.closeEntry();
            zis.close();
            System.out.println("Unzipped");
        } catch (IOException e){
            e.printStackTrace();
        }
        return "";
    }

    public static String sqlConnect(int systemID, String sql, String file) throws IOException {
        //initialize connection
        Connection connection;
        String systemName;
        try {
            //initialize connection to internal resource db in jar
            System.out.println("jdbc:sqlite:" + file);
            connection = DriverManager.getConnection("jdbc:sqlite:" + file);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(10);

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, systemID);
            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()) {
                systemName = rs.getString("solarSystemName");
                return systemName;
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
        return "";
    }
}
