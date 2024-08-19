package net.axiiom.CoordinatesBook.Book;

import net.axiiom.CoordinatesBook.Coordinate.Coordinate;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Location;

import java.util.List;
import java.util.Objects;

/*
    Builds a book in a form that is displayable user based off of the Coordinate class
 */
public class BookBuilder
{
    // Builds a page cooresponding to an inputted coordinate
    public static ComponentBuilder buildCoordinatePage(Coordinate _coordinate)
    {
        // Get Location information: World, X, Y, Z
        final String coordinateUuid = _coordinate.getUuid();
        final Location coordinateLocation = _coordinate.getLocation();
        final String worldName = Objects.requireNonNull(coordinateLocation.getWorld()).getName();
        final String dimension = _coordinate.getWorldName();
        final String location = coordinateLocation.getBlockX() + " " + coordinateLocation.getBlockY() + " " + coordinateLocation.getBlockZ();

        /*
            The following lines generate all the commands to be input into the book.
            These commands are embedded within the text in the book.
         */

        // Gets the coordinate name and truncates it to the maximum number of characters that can be seen on a single line in the game
        final String coordName = _coordinate.getName().trim();
        final String coordNameTrunc = truncate(coordName,19);

        // Generates the command to teleport to this coordinate
        final String travelCommand = String.format("/fasttravel %s %s", location, worldName);
        final String travelHover = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "TRAVEL here";

        // Generates the command to set the compass target to this coordinate
        final String compassCommand = String.format("/compasstarget %s %s", location, worldName);
        final String compassHover = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "SET your compass target";

        // Generates the command to share the coordinate with another player
        final String shareCommand = String.format("/sharecoordinate %s", coordinateUuid);
        final String shareHover = ChatColor.BLUE + "" + ChatColor.BOLD + "SHARE this coordinate";

        // Generates the command to delete the coordinate
        final String deleteCommand = String.format("/removecoordinate %s", coordinateUuid);
        final String deleteHover = String.format(ChatColor.RED + "" + ChatColor.BOLD + "DELETE this coordinate");

        // Generates the command to delete the coordinate
        final String renameCommand = String.format("/renamecoordinate %s", coordinateUuid);
        final String renameHover = String.format(ChatColor.GOLD + "" + ChatColor.BOLD + "RENAME this coordinate");

        // This is a Spigot API defined class that builds text with embedded commands
        ComponentBuilder page = new ComponentBuilder(coordNameTrunc + "\n\n");

        // Adds the Dimension and Location to the current page
        page.append("Dimension: ").color(ChatColor.BLACK)
        .append(dimension + "\n").color(ChatColor.DARK_AQUA).create();
        page.append("Loc: ").color(ChatColor.BLACK)
          .append(location.replaceAll(" ","/") + "\n\n").color(ChatColor.DARK_AQUA).create();

        // Adds the Delete command that is run when the user clicks the text
        page.append("> ").bold(true).color(ChatColor.GRAY)
          .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, renameCommand))
          .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(renameHover).create()))
          .append("Rename\n").color(ChatColor.GOLD).bold(false)
          .create();

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
        page.append("\n\n\n\n<- Table of Contents").color(ChatColor.DARK_GRAY).italic(true)
            .event(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, 1 + ""))
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(ChatColor.GRAY + "" + ChatColor.ITALIC
                            + "Go back to the Table of Contents").create()))
            .create();

        return page;
    }

    // Generates the Table of Contents page - the user can click the text on this page display the desired coordinate
    public static ComponentBuilder getTableOfContents(List<Coordinate> coordinates) {
        ComponentBuilder tableOfContents = new ComponentBuilder("Table Of Contents\n\n");
        String hoverString = ChatColor.GRAY + "" + ChatColor.ITALIC + "Go to %s's page";

        int pageIndex = 2;
        for(Coordinate coord : coordinates) {
            tableOfContents.append(pageIndex-1 + ") ").color(ChatColor.BLACK)
                .append(truncate(coord.getName(),16) +"\n").color(ChatColor.DARK_AQUA)
                .event(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, pageIndex + ""))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.format(hoverString,coord.getName())).create()))
                .create();

            pageIndex++;
        }


        return tableOfContents;
    }

    // Truncates the string to a max length of 19
    private static String truncate(String _input, int _allowedLength) {
        for(char c : _input.toCharArray()) if(c == 'i') _allowedLength += 1;
        return _allowedLength < _input.length() ? _input.substring(0,_allowedLength) : _input;
    }
}
