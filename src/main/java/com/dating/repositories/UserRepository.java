package com.dating.repositories;

import com.dating.models.PostalInfo;
import com.dating.models.users.Admin;
import com.dating.models.users.DatingUser;
import com.dating.viewModels.datingUser.PreviewDatingUser;
import com.dating.viewModels.datingUser.ViewProfileDatingUser;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;

public class UserRepository
{
    Connection lovestruckConnection = null;
    Connection favouriteslistConnection = null;

    DatingUser loggedInDatingUser = new DatingUser();
    Admin loggedInAdmin = new Admin();
    
    
    /**
     * Laver en connection til lovestruck-databasen
     *
     * @param dbName Navnet på db som vi connecter til
     *
     * @return Connection Den oprettede connection ELLER null ved fejl i oprettelsen af connection
     */
    public Connection establishConnection(String dbName)
    {
        Connection connection = null;
        try
        {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/"+ dbName +
                    "?serverTimezone=UTC", "gruppe10", "gruppe10");
        }
        catch(SQLException e)
        {
            System.out.println("Error in establishConnection: " + e.getMessage());
        }
        
        return connection;
    }

    // TODO: skal vi bruge billedmetoderne?
    /*
    public void writePictureToDb(int idDatingUser)
    {
        lovestruckConnection = establishConnection("lovestruck");

        try
        {
            PreparedStatement preparedStatement = lovestruckConnection.prepareStatement("UPDATE dating_users SET profilepicture = ? WHERE id_dating_user = ?");

            InputStream inputStream = new FileInputStream("C:\\Users\\rasmu\\IdeaProjects\\Lovestruck\\src\\main\\resources\\static\\image\\textLogo.png");

            preparedStatement.set
            preparedStatement.setBlob(1, inputStream);
            preparedStatement.setInt(2, idDatingUser);

            preparedStatement.executeUpdate();



        } catch(Exception e)
        {
            System.out.println("Error in writePictureToDb: " + e.getMessage());
        }
    }
    public void readPictureFromDb(int idDatingUser, String fileName)
    {
        lovestruckConnection = establishConnection("lovestruck");

        try {
            PreparedStatement preparedStatement = lovestruckConnection.prepareStatement
                                   ("SELECT profile_picture FROM dating_users WHERE id_dating_user = ?");

            preparedStatement.setInt(1, idDatingUser);

            ResultSet resultSet = preparedStatement.executeQuery();

            File file = new File("src\\main\\resources\\static\\image\\profilepictures\\" +fileName);

            FileOutputStream outputStream = new FileOutputStream(file);


            //String filePath = "" + file.getAbsolutePath(); gemmer fils path
            System.out.println("Writing to file: " + file.getAbsolutePath());

            while (resultSet.next())
            {
                InputStream inputStream = resultSet.getBinaryStream("profile_picture");
                byte[] buffer = new byte[128];
                while (inputStream.read(buffer) > 0)
                {
                    outputStream.write(buffer);
                }
            }

            }
            catch(Exception exception)
            {
                System.out.println("Error in readPictureFromDb: " + exception.getMessage());
            }

    }
    
     */
    
    
    /**
     * Tilføjer DatingUser-objekt til dating_users-tabellen i db
     * OG sætter dets id-attribut
     * OG opretter ny favourites_list-tabel i db knyttet til brugeren
     *
     * @param datingUser DatingUser-obj. som skal tilføjes til db
     */
    public void addDatingUserToDb(DatingUser datingUser)
    {
        lovestruckConnection = establishConnection("lovestruck");
        try
        {
            Blob profilePicture = new SerialBlob(datingUser.getProfilePictureBytes());
            int idPostalInfo = findIdPostalInfoFromPostalInfoObject(datingUser.getPostalInfo());
            
         
            String sqlCommand = "INSERT into dating_users(blacklisted, sex, interested_in, profile_picture, age, " +
                                        "username, email, password, id_postal_info) " +
                    "values (?,?,?,?,?,?,?,?,?);";
            
            // det er vores SQL sætning som vi beder om at få prepared til at blive sendt til databasen:
            PreparedStatement preparedStatement = lovestruckConnection.prepareStatement(sqlCommand);
            
            preparedStatement.setInt(1, datingUser.convertBooleanToInt(datingUser.isBlacklisted()));
            preparedStatement.setInt(2, datingUser.convertBooleanToInt(datingUser.getSex()));
            preparedStatement.setInt(3, datingUser.getInterestedIn());
            preparedStatement.setBlob(4, profilePicture);
            preparedStatement.setInt(5, datingUser.getAge());
            preparedStatement.setString(6, datingUser.getUsername());
            preparedStatement.setString(7, datingUser.getEmail());
            preparedStatement.setString(8, datingUser.getPassword());
            preparedStatement.setInt(9, idPostalInfo);
            
            // user tilføjes til database
            preparedStatement.executeUpdate();
            
            // nu hvor user er blevet oprettet, tilføjer vi databasens genererede id_dating_user til datingUser-objektet
            int idDatingUser = retrieveDatingUserIdFromDb(datingUser);
            
            if(idDatingUser!=-1)
            {
                // setter id'et, hvis det er genereret korrekt
                datingUser.setIdDatingUser(idDatingUser);
                // genererer en favourites_list tabel i databasen - knyttet til user-entitet via id_dating_user
                // fx til en user med id_dating_user 3 oprettes tabellen: favourites_list_3
                createFavouritesListTableDb(idDatingUser);
                loggedInDatingUser = datingUser;
            }
        }
        catch(Exception e)
        {
            System.out.println("Error in addDatingUserToDb: " + e.getMessage());
        }
    }
    
    /**
     * Opretter ny favourites_list tabel ud fra idDatingUser-int
     *
     * @param idDatingUser id'et som skal være i db-tabellens navn: fx favourites_list_3
     */
    public void createFavouritesListTableDb(int idDatingUser)
    {
        try
        {
            favouriteslistConnection = establishConnection("lovestruck_favourites_list");
            
            // lovestruc_favourites_list == den database som vi laver tabellen i
            // favourites_list_? == navnet på tabellen
            // id_dating_user INT NOT NULL,== navn på ny kolonne og hvilke bokse der er krydset af
            // PRIMARY KEY(id_dating_user) == siger at det er kolonnen id_dating_user som er primary key
            String sqlCommand = "CREATE TABLE lovestruck_favourites_list.favourites_list_? (id_dating_user INT NOT NULL, PRIMARY " +
                    "KEY (id_dating_user));";
            
            PreparedStatement preparedStatement = favouriteslistConnection.prepareStatement(sqlCommand);
            
            preparedStatement.setInt(1, idDatingUser);
            
            preparedStatement.executeUpdate();
        }
        catch(SQLException e)
        {
            System.out.println("Error in createFavouritesListTableDb: " + e.getMessage());
        }
        
    }
    
    /**
     * Tilføjer DatingUser-obj.til datingUser's favouritesList
     * DatingUser-obj. som tilføjes hentes fra den sidste plads i loggedInDatingUser's favouritesList
     *
     * @param datingUser Den bruger som skal tilføje anden bruger til sin favouritesList
     */
    public void addDatingUserToFavouritesListInDb(DatingUser datingUser)
    {
        favouriteslistConnection = establishConnection("lovestruck_favourites_list");
        
        // hvis der ER noget på datingUser-obj.s favouritesList
        if(datingUser.getFavouritesList() != null)
        {
            // find sidste index plads' nummer
            int lastIndex = datingUser.getFavouritesList().size() - 1;
    
            // find favouritesList -- find datingUser-obj. på sidste index -- find id'et på denne
            int idDatingUserToAdd = datingUser.getFavouritesList().get(lastIndex).getIdDatingUser();
    
            try
            {
                String sqlCommand = "INSERT into favourites_list_? (id_dating_user) values(?)";
        
                PreparedStatement preparedStatement = favouriteslistConnection.prepareStatement(sqlCommand);
        
                preparedStatement.setInt(1, datingUser.getIdDatingUser());
                preparedStatement.setInt(2, idDatingUserToAdd);
        
                preparedStatement.executeUpdate();
        
            }
            catch(SQLException e)
            {
                System.out.println("Error in addDatingUserToFavouritesListInDb: " + e.getMessage());
            }
        }
        
    }
    
    /**
     * Fjerner DatingUser-obj.fra datingUser's favouritesList
     *
     * @param datingUserTableToUpdate Den bruger som skal fjerne anden bruger til sin favouritesList
     * @param datingUserToRemove Den bruger som skal fjernes fra listen
     */
    public void removeDatingUserToFavouritesListInDb(DatingUser datingUserTableToUpdate,
                                                     DatingUser datingUserToRemove)
    {
        favouriteslistConnection = establishConnection("lovestruck_favourites_list");
        
        try
        {
            String sqlCommand = "DELETE from favourites_list_? WHERE id_dating_user = ?";
        
            PreparedStatement preparedStatement = favouriteslistConnection.prepareStatement(sqlCommand);
        
            preparedStatement.setInt(1, datingUserTableToUpdate.getIdDatingUser());
            preparedStatement.setInt(2, datingUserToRemove.getIdDatingUser());
        
            preparedStatement.executeUpdate();
        
        }
        catch(SQLException e)
        {
            System.out.println("Error in removeDatingUserToFavouritesListInDb: " + e.getMessage());
        }
    }
    
    /**
     * Finder id_dating_user-værdien gemt i database på et givent user-objekt
     *
     * @param datingUser user-objektet som vi finder id_dating_user-værdien på/til
     
     *
     * @return int id_dating_user-værdien som er fundet - -1 hvis ikke fundet
     */
    public int retrieveDatingUserIdFromDb(DatingUser datingUser)
    {
        lovestruckConnection = establishConnection("lovestruck");
        
        int idDatingUser = -1;
        
        try
        {
            String sqlCommand = "SELECT * FROM dating_users WHERE username like ?";
            
            // det er vores SQL sætning som vi beder om at få prepared til at blive sendt til databasen:
            PreparedStatement preparedStatement = lovestruckConnection.prepareStatement(sqlCommand);
            
            preparedStatement.setString(1, "%" + datingUser.getUsername() + "%");
            
            ResultSet resultSet = preparedStatement.executeQuery();
            
            
            // TODO Find ud af hvorfor vi skal skrive next
            if(resultSet.next())
            {
                idDatingUser = resultSet.getInt(1);
            }
            
            
            
            //System.out.println(idDatingUser);
        }
        catch(SQLException e)
        {
            System.out.println("Error in retrieveIdDatingUserFromDb: " + e.getMessage());
        }
        
        return idDatingUser;
    }
    
    /**
     * Tjekker om username allerede er gemt på anden user i db
     *
     * @param username Username som tjekkes for om den er optaget
     
     *
     * @return Boolean Svaret på om username'et er ledig
     */
    public boolean isUsernameAvailable(String username)
    {
        lovestruckConnection = establishConnection("lovestruck");
        
        boolean usernameIsAvailable = true; // sættes til at være available by default
        
        try
        {
            String sqlCommand = "SELECT * FROM dating_users WHERE username = ?";
            
            // det er vores SQL sætning som vi beder om at få prepared til at blive sendt til databasen:
            PreparedStatement preparedStatement = lovestruckConnection.prepareStatement(sqlCommand);
            
            preparedStatement.setString(1, username);
            
            ResultSet resultSet = preparedStatement.executeQuery();
            
            // TODO Find ud af hvorfor vi skal skrive next
            if(resultSet.next())
            {
                usernameIsAvailable = false;
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error in isUsernameAvailable: " + e.getMessage());
        }
        
        return usernameIsAvailable;
    }
    
    /**
     * Tjekker om email allerede er gemt på anden user i db
     *
     * @param email Email som tjekkes for om den er optaget
     
     *
     * @return Boolean Svaret på om email'en er ledig
     */
    public boolean isEmailAvailable(String email)
    {
        lovestruckConnection = establishConnection("lovestruck");
        
        boolean emailIsAvailable = true; // sættes til at være available by default
        
        try
        {
            String sqlCommand = "SELECT * FROM dating_users WHERE email = ?";
            
            // det er vores SQL sætning som vi beder om at få prepared til at blive sendt til databasen:
            PreparedStatement preparedStatement = lovestruckConnection.prepareStatement(sqlCommand);
            
            preparedStatement.setString(1, email);
            
            ResultSet resultSet = preparedStatement.executeQuery();
            
            // TODO Find ud af hvorfor vi skal skrive next
            if(resultSet.next())
            {
                emailIsAvailable = false;
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error in isEmailAvailable: " + e.getMessage());
        }
        
        return emailIsAvailable;
    }
    
    /**
     * Tjekker om bruger som prøver på at logge ind findes i enten admins eller dating_users tabel i db
     *
     * @param dataFromLogInForm WebRequest som bruges til at hente data fra login-form
     *
     * @return User Returnerer enten NULL hvis user ikke findes i db - eller enten admin eller datingUser
     */
    /*public User checkIfUserExists(WebRequest dataFromLogInForm)
    {
        lovestruckConnection = establishConnection("lovestruck");
        
        try
        {
            ResultSet resultSet = findUserInDb(dataFromLogInForm, "admins");
            
            if(resultSet.next()) // hvis det er en admin
            {
                loggedInUser = loggedInAdmin;

                loggedInUser.setUsername(resultSet.getString(3));
                loggedInUser.setEmail(resultSet.getString(4));
                loggedInUser.setPassword(resultSet.getString(5));
              
            }
            else // når det ikke er en admin, så tjekker vi om det er en datingUser
            {
                resultSet = findUserInDb(dataFromLogInForm, "dating_users");

                if(resultSet.next()) // hvis det er en datingUser
                {
                    loggedInUser = loggedInDatingUser;

                    loggedInUser.setUsername(resultSet.getString(3));
                    loggedInUser.setEmail(resultSet.getString(4));
                    loggedInUser.setPassword(resultSet.getString(5));
                   
                }
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error in isEmailAvailable: " + e.getMessage());
        }
    
        return loggedInUser;
    }
    
     */
    
    /**
     * Tjekker via data fra dataFromLogInForm om der er en tilsvarende Admin-bruger i admin-db
     *
     * @param dataFromLogInForm Den form som indeholde log ind dataen som skal bruges
     *
     * @return Admin Returnerer et Admin-obj. som enten (hvis den ikke fandtes i db) har ingen værdier eller ()
     * er blevet
     * tildelt væri
     */
    public Admin checkIfUserExistsInAdminsTable(WebRequest dataFromLogInForm)
    {
        lovestruckConnection = establishConnection("lovestruck");
        
        try
        {
            ResultSet resultSet = findUserInDb(dataFromLogInForm, "admins");
            
            if(resultSet.next()) // hvis admin er fundet i db
            {
                loggedInAdmin.setUsername(resultSet.getString(3));
                loggedInAdmin.setEmail(resultSet.getString(4));
                loggedInAdmin.setPassword(resultSet.getString(5));
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error in isEmailAvailable: " + e.getMessage());
        }
        
        return loggedInAdmin;
    }
    
    public DatingUser checkIfUserExistsInDatingUsersTable(WebRequest dataFromLogInForm)
    {
        ResultSet resultSet = findUserInDb(dataFromLogInForm, "dating_users");
        
        loggedInDatingUser = createDatingUserFromResultSet(resultSet);
        
        return loggedInDatingUser;
    }
    
    /**
     * Finder en user i valgfri tabel ud fra username og password
     *
     * @param dataFromLogInForm WebRequest som bruges til at hente data fra login-form
     * @param table tabel user findes i
     *
     * @return ResultSet Fundet user-entitet er gemt i ResultSettet - ALDRIG null, MEN tom, hvis user ikke findes i
     * tabel
     */
    public ResultSet findUserInDb(WebRequest dataFromLogInForm, String table)
    {
        lovestruckConnection = establishConnection("lovestruck");
        
        ResultSet resultSet = null;
        try
        {
            String sqlCommand = "SELECT * FROM " + table +" WHERE username = ? AND password = ? ";
            
            // det er vores SQL sætning som vi beder om at få prepared til at blive sendt til databasen:
            PreparedStatement preparedStatement = lovestruckConnection.prepareStatement(sqlCommand);
            
            preparedStatement.setString(1, dataFromLogInForm.getParameter("usernameinput"));
            preparedStatement.setString(2, dataFromLogInForm.getParameter("passwordinput"));
            
            resultSet = preparedStatement.executeQuery();
        }
        catch(SQLException e)
        {
            System.out.println("Error in findUserInDb: " + e.getMessage());
        }
        return resultSet;
    }
    
    // Overload
    public ResultSet findUserInDb(int idDatingUser)
    {
        lovestruckConnection = establishConnection("lovestruck");
    
        ResultSet resultSet = null;
        try
        {
            String sqlCommand = "SELECT * FROM dating_users WHERE id_dating_user = ?";
    
            // det er vores SQL sætning som vi beder om at få prepared til at blive sendt til databasen:
            PreparedStatement preparedStatement = lovestruckConnection.prepareStatement(sqlCommand);
    
            preparedStatement.setInt(1, idDatingUser);
    
            resultSet = preparedStatement.executeQuery();
        }
        catch(SQLException e)
        {
            System.out.println("Error in findUserInDb: " + e.getMessage());
        }
        return resultSet;
    }
    
    public DatingUser retrieveDatingUserFromDb(int idDatingUser)
    {
        ResultSet resultSet = findUserInDb(idDatingUser);
    
        return createDatingUserFromResultSet(resultSet);
    }
    
    
    public ViewProfileDatingUser findDatingUserInDbToView(int idDatingUser)
    {
        // opretter viewProfileDatingUser som returneres
        ViewProfileDatingUser viewProfileDatingUser = null;
    
        // finder DatingUser i db ud fra idDatingUser og gemmer i resultSet
        ResultSet resultSet = findUserInDb(idDatingUser);
    
        // opretter datingUser-obj. ud fra resultSet
        DatingUser datingUser = null;
        
        datingUser = createDatingUserFromResultSet(resultSet);
        
        // konverterer datingUser-obj. til viewProfileDatingUser-obj. - som gemmes i retur-variablen
        viewProfileDatingUser = datingUser.convertDatingUserToViewProfileDatingUser();
      
        return viewProfileDatingUser;
    }
    
    
    // TODO HER
    public void updateLoggedInDatingUserInDb(DatingUser loggedInDatingUser)
    {
        lovestruckConnection = establishConnection("lovestruck");
        
        // sætter postalId
        int idPostalInfo = findIdPostalInfoFromPostalInfoObject(loggedInDatingUser.getPostalInfo());
        String tagsListString = loggedInDatingUser.convertTagsListToString();
        
        try
        {
            Blob profilePictureBlob = new SerialBlob(loggedInDatingUser.getProfilePictureBytes());
            
            // TODO: tilføj: image_path som kolonne i database - og så tilføj den sqlCommanden her
            String sqlCommand = "UPDATE dating_users SET interested_in = ?, " +
                                        "username = ?, " +
                                        "email = ?, " +
                                        "age = ?, " +
                                        "id_postal_info = ?, " +
                                        "password = ?, " +
                                        "description = ?, " +
                                        "tags = ?, " +
                                        "profile_picture = ?" +
                                        "WHERE id_dating_user = ?";
        
            // det er vores SQL sætning som vi beder om at få prepared til at blive sendt til databasen:
            PreparedStatement preparedStatement = lovestruckConnection.prepareStatement(sqlCommand);
        
            preparedStatement.setInt(1, loggedInDatingUser.getInterestedIn());
            preparedStatement.setString(2, loggedInDatingUser.getUsername());
            preparedStatement.setString(3, loggedInDatingUser.getEmail());
            preparedStatement.setInt(4, loggedInDatingUser.getAge());
            preparedStatement.setInt(5, idPostalInfo);
            preparedStatement.setString(6, loggedInDatingUser.getPassword());
            preparedStatement.setString(7, loggedInDatingUser.getDescription());
            preparedStatement.setString(8, tagsListString);
            preparedStatement.setBlob(9, profilePictureBlob);
            preparedStatement.setInt(10, loggedInDatingUser.getIdDatingUser());
        
            // user tilføjes til database
            preparedStatement.executeUpdate();
        }
        catch(SQLException e)
        {
            System.out.println("Error in updateLoggedInDatingUserInDb: " + e.getMessage());
    
        }
    }
    
    //------------------ POSTALINFO METODER -------------------//
    
    /**
     * Finder PostalInfo-entitet knyttet til bestemt idPostalInfo
     *
     * @param id idPostalInfo som PostalInfo-entitet findes ud fra
     *
     * @return PostalInfo Returnerer PostalInfo-entiteten omdannet til postalInfo-obj
     */
    public PostalInfo findPostalInfoObjectFromIdPostalInfo(int id)
    {
        lovestruckConnection = establishConnection("lovestruck");
        
        PostalInfo postalInfo = null;
        
        try
        {
            String sqlCommand = "SELECT * FROM postal_info WHERE id_postal_info = ?";
            
            // det er vores SQL sætning som vi beder om at få prepared til at blive sendt til databasen:
            PreparedStatement preparedStatement = lovestruckConnection.prepareStatement(sqlCommand);
            
            preparedStatement.setInt(1, id);
            
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(resultSet.next()) // hvis der IKKE ligger noget i resultSettet sættes det til null
            {
                postalInfo = new PostalInfo(resultSet.getInt(2), resultSet.getString(3));
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error in findPostalInfoObjectFromIdPostalInfo: " + e.getMessage());
        }
        
        return postalInfo;
    }
    
    /**
     * Finder idPostalInfo knyttet til et givent postInfo-obj i db
     *
     * @param postalInfo PostalInfo-objektet som tilsvarende id findes til
     *
     * @return int IdPostalInfo-værdien som er fundet i db
     */
    public int findIdPostalInfoFromPostalInfoObject(PostalInfo postalInfo)
    {
        if(postalInfo != null)
        {
    
            lovestruckConnection = establishConnection("lovestruck");
    
            int idPostalInfo = 0;
    
            try
            {
                String sqlCommand = "SELECT * FROM postal_info WHERE zip_code = ?";
        
                // det er vores SQL sætning som vi beder om at få prepared til at blive sendt til databasen:
                PreparedStatement preparedStatement = lovestruckConnection.prepareStatement(sqlCommand);
        
                preparedStatement.setInt(1, postalInfo.getZipCode());
        
                ResultSet resultSet = preparedStatement.executeQuery();
        
                if(resultSet.next()) // hvis der IKKE ligger noget i resultSettet sættes det til null
                {
                    idPostalInfo = resultSet.getInt(1);
                }
            }
            catch(SQLException e)
            {
                System.out.println("Error in findIdPostalInfoFromPostalInfoObject: " + e.getMessage());
            }
    
    
            return idPostalInfo;
        }
        return -1;
    }
    
    public PostalInfo findPostalInfoObjectFromZipCodeInput(int zipCode)
    {
        lovestruckConnection = establishConnection("lovestruck");
        
        PostalInfo postalInfo = null;
    
        try
        {
            String sqlCommand = "SELECT * FROM postal_info WHERE zip_code = ?";
        
            // det er vores SQL sætning som vi beder om at få prepared til at blive sendt til databasen:
            PreparedStatement preparedStatement = lovestruckConnection.prepareStatement(sqlCommand);
        
            preparedStatement.setInt(1, zipCode);
        
            ResultSet resultSet = preparedStatement.executeQuery();
        
            if(resultSet.next()) // hvis der IKKE ligger noget i resultSettet sættes det til null
            {
                postalInfo = new PostalInfo(resultSet.getInt(2), resultSet.getString(3));
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error in findPostalInfoObjectFromZipCodeInput: " + e.getMessage());
        }
    
        return postalInfo;
    }
    
    public boolean checkIfValidZipCode(int zipCodeInput)
    {
        boolean doesZipCodeExitsInDb = false;
    
        lovestruckConnection = establishConnection("lovestruck");
    
        PostalInfo postalInfo = null;
    
        try
        {
            String sqlCommand = "SELECT * FROM postal_info WHERE zip_code = ?";
        
            // det er vores SQL sætning som vi beder om at få prepared til at blive sendt til databasen:
            PreparedStatement preparedStatement = lovestruckConnection.prepareStatement(sqlCommand);
        
            preparedStatement.setInt(1, zipCodeInput);
        
            ResultSet resultSet = preparedStatement.executeQuery();
        
            if(resultSet.next()) // hvis der IKKE ligger noget i resultSettet sættes det til null
            {
                doesZipCodeExitsInDb = true;
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error in checkIfValidZipCode: " + e.getMessage());
        }
    
        return doesZipCodeExitsInDb;
    }
    
    /**
     * Laver ArrayList med PreviewDatingUser-objekter ud fra ALLE datingUsers i tabel (undtaget
     * datingUser-obj med idDatingUser)
     *
     * @param idDatingUser id som henviser til bruger der IKKE skal gemmes på listen
     *
     * @return ArrayList<PreviewDatingUser> Returnerer liste med ALLE PreviewDatingUser's
     */
    public ArrayList<PreviewDatingUser> createListOfAllDatingUsersFromDb(int idDatingUser)
    {
        lovestruckConnection = establishConnection("lovestruck");
    
        ArrayList<PreviewDatingUser> datingUsersList = new ArrayList<>();
    
        try
        {
            String sqlCommand = "SELECT * FROM dating_users WHERE NOT id_dating_user = ?";
        
            // det er vores SQL sætning som vi beder om at få prepared til at blive sendt til databasen:
            // henter ALLE datingUsers fra tabel BORTSET fra den der er logget ind
            PreparedStatement preparedStatement = lovestruckConnection.prepareStatement(sqlCommand);
        
            preparedStatement.setInt(1, idDatingUser);
        
            ResultSet resultSet = preparedStatement.executeQuery();
        
            while(resultSet.next()) // hvis der IKKE ligger noget i resultSettet sættes det til null
            {
                PreviewDatingUser previewDatingUser = createPreviewDatingUserFromResultSet(resultSet);
                datingUsersList.add(previewDatingUser);
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error in findPostalInfoObjectFromZipCodeInput: " + e.getMessage());
        }
        return datingUsersList;
    }
    
    //------------------ IKKE DB-METODER -------------------//
    
    /**
     * Nulstiller loggedInAdmin og loggedInDatingUser-klassevariabel
     *
     * @return void
     */
    public void setLoggedInUserToNull()
    {
        loggedInAdmin = null;
        loggedInDatingUser = null;
    }
    
    /**
     * Opretter PreviewDatingUser-obj ud fra entitet på resultSet
     *
     * @param resultSet ResultSet som PreviewDatingUser-obj dannes ud fra
     *
     * @return PreviewDatingUser Returnerer det oprettede PreviewDatingUser-obj
     */
    public PreviewDatingUser createPreviewDatingUserFromResultSet(ResultSet resultSet)
    {
        PreviewDatingUser previewDatingUser = null;
        try
        {
            Blob profilePictureBlob = resultSet.getBlob(9);
            byte[] profilePictureBytes = profilePictureBlob.getBytes(1, (int) profilePictureBlob.length());
            
            
            // TODO: skal det være noget andet end inputstream??
            previewDatingUser = new PreviewDatingUser(resultSet.getInt(1), profilePictureBytes,
                    resultSet.getString(3),
                    resultSet.getInt(6));
        }
        catch(SQLException e)
        {
            System.out.println("Error in createPreviewDatingUserFromResultSet: " + e.getMessage());
        }
        return previewDatingUser;
    }
    
    /**
     * Opretter DatingUser-obj ud fra resultSet
     *
     * @param resultSet ResultSet som DatingUser-obj dannes ud fra
     *
     * @return DatingUser Returnerer det oprettede DatingUser-obj
     */
    public DatingUser createDatingUserFromResultSet(ResultSet resultSet)
    {
        DatingUser datingUser = new DatingUser();
        try
        {
            if(resultSet.next())
            {
                Blob profilePictureBlob = resultSet.getBlob(9);
                byte[] profilePictureBytes = profilePictureBlob.getBytes(1, (int) profilePictureBlob.length());
                ArrayList<DatingUser> favouritesList =
                        convertResultSetToFavouritesList(retrieveFavouritesList(resultSet.getInt(1)));
                
                datingUser.setIdDatingUser(resultSet.getInt(1));
                datingUser.setBlacklisted(datingUser.convertIntToBoolean(resultSet.getInt(2)));
                datingUser.setUsername(resultSet.getString(3));
                datingUser.setEmail(resultSet.getString(4));
                datingUser.setPassword(resultSet.getString(5));
                datingUser.setAge(resultSet.getInt(6));
                datingUser.setSex(datingUser.convertIntToBoolean(resultSet.getInt(7)));
                datingUser.setInterestedIn(resultSet.getInt(8));
                datingUser.setProfilePictureBytes(profilePictureBytes);
                datingUser.setDescription(resultSet.getString(10));
                datingUser.setTagsList(datingUser.convertStringToTagsList(resultSet.getString(11)));
                datingUser.setPostalInfo(findPostalInfoObjectFromIdPostalInfo(resultSet.getInt(12)));
                datingUser.setFavouritesList(favouritesList);
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error in createDatingUserFromResulSet: " + e.getMessage());
        }
        
        return datingUser;
    }
 
    
    public ResultSet retrieveFavouritesList(int idDatingUser)
    {
        ResultSet resultSet = null;
    
        favouriteslistConnection = establishConnection("lovestruck_favourites_list");
        
        try
        {
            String sqlCommand = "SELECT * FROM lovestruck_favourites_list.favourites_list_?;";
        
            PreparedStatement preparedStatement = favouriteslistConnection.prepareStatement(sqlCommand);
        
            preparedStatement.setInt(1, idDatingUser);
        
            resultSet = preparedStatement.executeQuery();
        
        }
        catch(SQLException e)
        {
            System.out.println("Error in updateFavouritesListInDb: " + e.getMessage());
        }
        return resultSet;
    }
    
    public ArrayList<DatingUser> convertResultSetToFavouritesList(ResultSet resultSet)
    {
        ArrayList<DatingUser> favouritesList = new ArrayList<>();
        try
        {
            while(resultSet.next())
            {
                favouritesList.add(retrieveDatingUserFromDb(resultSet.getInt(1)));
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error in convertResultSetToFavouritesList: " + e.getMessage());
        }
        
        return favouritesList;
    }
    
    
}
