package net.axiiom.CoordinatesBook.utilities;

import net.axiiom.CoordinatesBook.features.Coordinate;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/*
    Builds a book in a form that is displayable user based off of the Coordinate class
 */
public class BookBuilder
{
    private List<String> coordinateNameList;

    public BookBuilder() {
        coordinateNameList = new ArrayList<>();
    }

    // Builds a page cooresponding to an inputted coordinate
    public ComponentBuilder buildCoordinatePage(Coordinate _coordinate, String _validatorUUID)
    {
        // Get Location information: World, X, Y, Z
        Location coordinateLocation = _coordinate.getLocation();
        String worldName = coordinateLocation.getWorld().getName();
        String dimension = "";
            if(worldName.contains("_nether")) dimension = "Nether";
            else if(worldName.contains("_end")) dimension = "The End";
            else dimension = "Overworld";

        // Turns the player location into a string in the form X/Y/Z
        String location = coordinateLocation.getBlockX() + "/" + coordinateLocation.getBlockY() + "/" + coordinateLocation.getBlockZ();

        /*
            The following lines generate all the commands to be input into the book.
            These commands are embedded within the text in the book.
         */

        // Gets the coordinate name and truncates it to the maximum number of characters that can be seen on a single line in the game
        String coordName = _coordinate.getDescription().trim();
        String coordNameTrunc = truncate(coordName,19);

        // Generates the command to teleport to this coordinate
        String travelCommand = String.format("/fasttravel %s %s %s", _validatorUUID, location.replaceAll("/", " "), worldName);
        String travelHover = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "TRAVEL here";

        // Generates the command to set the compass target to this coordinate
        String compassCommand = String.format("/compasstarget %s %s %s", _validatorUUID, location.replaceAll("/"," "), worldName);
        String compassHover = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "SET your compass target";

        // Generates the command to share the coordinate with another player
        String shareCommand = String.format("/sharecoordinate %s %s %s", _validatorUUID, location.replaceAll("/"," "),worldName); //"/share <validator> <coordinate>";
        String shareHover = ChatColor.BLUE + "" + ChatColor.BOLD + "Share this coordinate";

        // Generates the command to delete the coordinate
        String deleteCommand = String.format("/removecoordinate %s %s %s", _validatorUUID, location.replaceAll("/"," "),worldName);
        String deleteHover = String.format(ChatColor.RED + "" + ChatColor.BOLD + "DELETE this coordinate");


        // This is a Spigot API defined class that builds text with embedded commands
        ComponentBuilder page = new ComponentBuilder(coordNameTrunc + "\n\n");

        // Adds the Dimension and Location to the current page
        page.append("Dimension: ").color(ChatColor.BLACK).append(dimension + "\n").color(ChatColor.DARK_AQUA).create();
        page.append("Loc: ").color(ChatColor.BLACK).append(location + "\n\n").color(ChatColor.DARK_AQUA).create();

        // Adds the Teleport command that is run when the user clicks the text
        page.append("> ").bold(true).color(ChatColor.GRAY)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, travelCommand))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(travelHover).create()))
                .append("Travel\n").color(ChatColor.DARK_GREEN).bold(false)
                .create();

        // Adds the Set Compass Target command that is run when the user clicks the text
        page.append("> ").bold(true).color(ChatColor.GRAY)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, compassCommand))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(compassHover).create()))
                .append("Compass Target\n").color(ChatColor.LIGHT_PURPLE).bold(false)
                .create();

        // Adds the Share command that is run when the user clicks the text
        page.append("> ").bold(true).color(ChatColor.GRAY)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, shareCommand))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(shareHover).create()))
                .append("Share\n").color(ChatColor.BLUE).bold(false)
                .create();

        // Adds the Delete command that is run when the user clicks the text
        page.append("> ").bold(true).color(ChatColor.GRAY)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, deleteCommand))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(deleteHover).create()))
                .append("Delete").color(ChatColor.RED).bold(false)
                .create();

        // This adds the "Go Back to Table of Contents" text to the bottom of the page
        page.append("\n\n\n\n\n<- Table of Contents").color(ChatColor.DARK_GRAY).italic(true)
                .event(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, 1 + ""))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(ChatColor.GRAY + "" + ChatColor.ITALIC
                                + "Go back to the Table of Contents").create()))
                .create();

        this.coordinateNameList.add(coordName);
        return page;
    }

    // Generates the Table of Contents page - the user can click the text on this page display the desired coordinate
    public ComponentBuilder getTableOfContents() {
        ComponentBuilder tableOfContents = new ComponentBuilder("Table Of Contents\n\n");
        String hoverString = ChatColor.GRAY + "" + ChatColor.ITALIC + "Go to %s's page";

        int pageIndex = 2;
        for(String coord : this.coordinateNameList) {
            tableOfContents.append(pageIndex-1 + ") ").color(ChatColor.BLACK)
                    .append(truncate(coord,16) +"\n").color(ChatColor.DARK_AQUA)
                    .event(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, pageIndex + ""))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.format(hoverString,coord)).create()))
                    .create();

            pageIndex++;
        }


        return tableOfContents;
    }

    // Truncates the string to a max length of 19
    private String truncate(String _input, int _allowedLength) {
        for(char c : _input.toCharArray()) if(c == 'i') _allowedLength += 1;
        return _allowedLength < _input.length() ? _input.substring(0,_allowedLength) : _input;
    }
}
