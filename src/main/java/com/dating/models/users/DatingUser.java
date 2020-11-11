package com.dating.models.users;

import com.dating.models.PostalInfo;
import com.dating.viewModels.datingUser.EditDatingUser;
import com.dating.viewModels.datingUser.PreviewDatingUser;
import com.dating.viewModels.datingUser.ViewProfileDatingUser;
import org.apache.tomcat.util.http.fileupload.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;

public class DatingUser extends User
{
    // det er smart at have denne, fordi så skal vi ikke hente den fra databasen
    // (fx når brugeren skal tilgå sin favoritliste)
    private int idDatingUser;
    private boolean isBlacklisted; // 0 == false, 1 == true
    private boolean sex; // false == mænd, true == kvinder
    private int interestedIn; // 0 == mænd, 1 == kvinder, 2 == begge køn
    private int age;
    private byte[] profilePictureBytes;
    private String description;
    private ArrayList<String> tagsList;
    private PostalInfo postalInfo;
    private ArrayList<DatingUser> favouritesList;
    
    // constructors
    public DatingUser(){}
    public DatingUser(boolean sex, int interestedIn, int age, String username, String email, String password)
    {
        super(username, email, password); // den kalder superklassens constructor
        this.isBlacklisted = false; // en bruger er aldrig blacklisted til at starte med når de opretter sig
        this.sex = sex;
        this.interestedIn = interestedIn;
        this.age = age;
        // sættes til standard-billede
        profilePictureBytes = createGenericProfilePictureBytes();
        description = null;
        tagsList = new ArrayList<>();
        postalInfo = new PostalInfo(0,"");
        favouritesList = new ArrayList<>();
    }
    
    // getters + setters
    public int getIdDatingUser()
    {
        return idDatingUser;
    }
    public void setIdDatingUser(int idDatingUser)
    {
        this.idDatingUser = idDatingUser;
    }
    public boolean isBlacklisted()
    {
        return isBlacklisted;
    }
    public void setBlacklisted(boolean blacklisted)
    {
        this.isBlacklisted = blacklisted;
    }
    public boolean getSex()
    {
        return sex;
    }
    public void setSex(boolean sex)
    {
        this.sex = sex;
    }
    public int getInterestedIn()
    {
        return interestedIn;
    }
    public void setInterestedIn(int interestedIn)
    {
        this.interestedIn = interestedIn;
    }
    public int getAge()
    {
        return age;
    }
    public void setAge(int age)
    {
        this.age = age;
    }
    public byte[] getProfilePictureBytes()
    {
        return profilePictureBytes;
    }
    public void setProfilePictureBytes(byte[] profilePictureBytes)
    {
        this.profilePictureBytes = profilePictureBytes;
    }
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
    public ArrayList<String> getTagsList()
    {
        return tagsList;
    }
    public void setTagsList(ArrayList<String> tags)
    {
        this.tagsList = tags;
    }
    public PostalInfo getPostalInfo()
    {
        return postalInfo;
    }
    public void setPostalInfo(PostalInfo postalInfo)
    {
        this.postalInfo = postalInfo;
    }
    public ArrayList<DatingUser> getFavouritesList()
    {
        return favouritesList;
    }
    public void setFavouritesList(ArrayList<DatingUser> favouritesList)
    {
        this.favouritesList = favouritesList;
    }
    
    public String getUsername()
    {
        return super.getUsername();
    }
    public void setUsername(String username)
    {
        super.setUsername(username);
    }
    public String getEmail()
    {
        return super.getEmail();
    }
    public void setEmail(String email)
    {
        super.setEmail(email);
    }
    public String getPassword()
    {
        return super.getPassword();
    }
    public void setPassword(String password)
    {
        super.setPassword(password);
    }
    
  
    // ANDRE metoder
    /**
     * Konverterer boolean til integer-værdi
     *
     * @param booleanInput Bool'en som skal konverteres
     
     * @return int Den konverterede boolean
     */
    public int convertBooleanToInt(Boolean booleanInput)
    {
        int convertedBoolean = 0; // convertedBoolean er nu sat til false - 0 == false
        
        if(booleanInput)
        {
            convertedBoolean = 1;
        }
        
        return convertedBoolean;
    }
    
    /**
     * Konverterer boolean integer-værdi til bool
     *
     * @param intInput Int-værdi som skal konverteres
     
     * @return boolean Resultatet af den konverterede int
     */
    public boolean convertIntToBoolean(int intInput)
    {
        return intInput == 1;
    }
    
    /**
     * Siger at et objekt af klassen er en DatingUser - Overskriver superklassen User's metode som siger false
     *
     * @return boolean Returns altid true, hvis denne overskrevede metode kaldes - fordi det er en DatingUser
     */
    @Override
    public boolean isDatingUser()
    {
        return true;
    }
    
    // TODO: måske overflødig
    /**
     * Konverterer interestedIn-attribut til tilsvarende String
     *
     * @return String interestedIn-attributtens String-værdi
     */
    public String convertInterestedInToString()
    {
        if(interestedIn==0)
        {
            return "males";
        }
        else if(interestedIn==1)
        {
            return "females";
        }
        
        return "malesandfemales";
    }
    
    /**
     * Splitter String op ved # og lægger på ArrayList-tagsList
     *
     * @param tagsString String som splittes og lægges på listen
     
     * @return ArrayList<String> ArrayListe bestående af Strings som er dannet via at splitte tagsString ved #
     */
    public ArrayList<String> convertStringToTagsList(String tagsString)
    {
        ArrayList<String> tagsList = new ArrayList<>();
        
        if(tagsString!=null)
        {
            // splitter tagsString ved # og laver til String-array
            String[] stringArray = tagsString.split("#");
            
            // lægger stringArray over i tagsList-ArrayListen
            Collections.addAll(tagsList, stringArray);
            //fordi Stringen STARTER med et #, sætter den null ind på index 0 - derfor slettes index 0
            
            tagsList.remove(0);
            
            // tilføjer # foran hver string, fordi den sletter #'et siden den splitter ved #
            // TODO: find evt på en anden løsning end split"#" hvor den ikke sletter #'et
            addHashTag(tagsList);
        }
    
        return tagsList;
    }
    
    public void addHashTag(ArrayList<String> tagsList)
    {
        for(int i = 0; i < tagsList.size(); i++)
        {
            tagsList.set(i, "#" + tagsList.get(i));
            System.out.println("#" + tagsList.get(i));
        }
    }
    
    public String convertTagsListToString()
    {
        String tagsString = "";
        
        if(tagsList.size() > 0)
        {
            for(String tag : tagsList)
            {
                tagsString += tag;
            }
        }
       return tagsString;
    }
    
    public EditDatingUser convertDatingUserToEditDatingUser()
    {
        int zipCode = 0;
        String tagsListString = null;
        
        if(postalInfo!=null) // hvis der ER noget i postInfo
        {
            zipCode = postalInfo.getZipCode();
        }
        if(tagsList != null) // hvis der ER noget i tagsList
        {
            tagsListString = convertTagsListToString();
        }
        
        return new EditDatingUser(interestedIn, super.getUsername(), super.getEmail(), age, zipCode,
                "", "", profilePictureBytes, description, tagsListString);
    }
    
    public PreviewDatingUser convertDatingUserToPreviewDatingUser()
    {
        return new PreviewDatingUser(idDatingUser, profilePictureBytes, super.getUsername(), age, true);
        
    }
    
    // TODO lav denne
    public ViewProfileDatingUser convertDatingUserToViewProfileDatingUser()
    {
        
        String zipCodeAndCity;
        String tagsListString;
        String sexAndAge = convertSexToString() + ", " + age + " år";
    
        if(postalInfo!=null) // hvis der ER noget i postInfo
        {
            zipCodeAndCity = postalInfo.getZipCode() + ", " + postalInfo.getCity();
        }
        else
        {
            zipCodeAndCity = "By ukendt";
        }
    
        String descriptionInput;
        if(description != null) // hvis der ER noget i postInfo
        {
            descriptionInput = description;
        }
        else
        {
            descriptionInput = "Ingen beskrivelse tilgængelig...";
        }
        
        if(tagsList != null) // hvis der ER noget i tagsList
        {
            tagsListString = convertTagsListToString();
        }
        else
        {
            tagsListString = "Ingen tags";
        }
     
        return new ViewProfileDatingUser(idDatingUser, super.getUsername(), sexAndAge, zipCodeAndCity,
                descriptionInput, tagsListString, profilePictureBytes);
        
    }
    
    public void addDatingUserToFavouritesList(DatingUser datingUser)
    {
        if(favouritesList == null)
        {
            favouritesList = new ArrayList<>();
        }
        
        favouritesList.add(datingUser);
    }
    
    public void removeDatingUserFromFavouritesList(DatingUser datingUserToRemove)
    {
        
        for(int i = 0; i < favouritesList.size(); i++)
        {
            if(favouritesList.get(i).idDatingUser == datingUserToRemove.idDatingUser)
            {
                favouritesList.remove(i);
                break;
            }
        }
        
        
        /*// TODO: måske er det her den fejler
        favouritesList.remove(datingUser);
         */
    }
    
    
    public ArrayList<PreviewDatingUser> getFavouritesListAsPreviewDatingUsers()
    {
        ArrayList<PreviewDatingUser> previewDatingUserArrayList = new ArrayList<>();
        
        for(DatingUser datingUser : favouritesList)
        {
            previewDatingUserArrayList.add(datingUser.convertDatingUserToPreviewDatingUser());
        }
    
        return previewDatingUserArrayList;
    }
    
    
    public String convertSexToString()
    {
        if(sex == true)
        {
            return "Kvinde";
        }
        return "Mand";
    }
    
    public byte[] createGenericProfilePictureBytes()
    {
        byte[] genericProfilePictureBytes = new byte[0];
        
        try
        {
            File genericProfilePictureFile = new File("src\\main\\resources\\static\\image\\profilepictures" +
                                                              "\\genericProfileImage.png");
    
    
            genericProfilePictureBytes = Files.readAllBytes(genericProfilePictureFile.toPath());
        }
        catch(IOException e)
        {
            System.out.println("Error in createGenericProfilePictureBytes: " + e.getMessage());
        }
        
        return genericProfilePictureBytes;
    }
    
    public boolean isViewProfileDatingUserOnFavouritesList(int IdViewProfileDatingUser)
    {
        for(DatingUser datingUser : favouritesList)
        {
            if(datingUser.idDatingUser == IdViewProfileDatingUser)
            {
                return true;
            }
        }
        
        return false;
    }
    
}





/*
// lav seperate metoder til at finde relevant == username, email, password




public void opretKandidatListe(int idDatingUser)
{
    // sql-command: "create table kandidatList" + idDatingUser
}

public void tilføjBrugerTilKandidatListe(int idDatingUser, User targetUser)
{
    // findUserId(targetUser)
    // sql: "insert ? into ?"
    
    // 1.? == idTargetUser
    // 2.? == KandidatList+idDatingUser (== navnet på kandidatlisten)

public int findUserId(User user)
{
    // find idDatingUser på user-objekt
    // return
}

 */

 
