import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.xmltree.XMLTree;
import components.xmltree.XMLTree1;

/**
 * Program to convert an XML RSS (version 2.0) feed from a given URL into the
 * corresponding HTML output file.
 *
 * @author Shannon McKinney
 *
 */
public final class RSSAggregator2 {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private RSSAggregator2() {
    }

    /**
     * Finds the first occurrence of the given tag among the children of the
     * given {@code XMLTree} and return its index; returns -1 if not found.
     *
     * @param xml
     *            the {@code XMLTree} to search
     * @param tag
     *            the tag to look for
     * @return the index of the first child of type tag of the {@code XMLTree}
     *         or -1 if not found
     * @requires [the label of the root of xml is a tag]
     * @ensures <pre>
     * getChildElement =
     *  [the index of the first child of type tag of the {@code XMLTree} or
     *   -1 if not found]
     * </pre>
     */
    private static int getChildElement(XMLTree xml, String tag) {
        assert xml != null : "Violation of: xml is not null";
        assert tag != null : "Violation of: tag is not null";
        assert xml.isTag() : "Violation of: the label root of xml is a tag";

        //get number of children
        int num = xml.numberOfChildren();
        int index = 0;
        //continue loop while label is not found
        while ((index < num)
                && ((tag.compareTo(xml.child(index).label()) != 0))) {

            index++;

        }
        //if child is not found then return -1
        if (index == num) {
            index = -1;

        }
        //return child index, -1 if not found
        return index;
    }

    /**
     * Processes one news item and outputs one table row. The row contains three
     * elements: the publication date, the source, and the title (or
     * description) of the item.
     *
     * @param item
     *            the news item
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the label of the root of item is an <item> tag] and
     *           out.is_open
     * @ensures <pre>
     * out.content = #out.content *
     *   [an HTML table row with publication date, source, and title of news item]
     * </pre>
     */
    private static void processItem(XMLTree item, SimpleWriter out) {
        assert item != null : "Violation of: item is not null";
        assert out != null : "Violation of: out is not null";
        assert item.isTag() && item.label().equals("item") : ""
                + "Violation of: the label root of item is an <item> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        /*
         * pubDate column
         */

        //get pubDate child index
        int index = getChildElement(item, "pubDate");

        //if found, output date with tags
        if ((index != -1) && (item.child(index).numberOfChildren() > 0)) {
            out.println("<td>" + item.child(index).child(0).label() + "</td>");
        } else {
            out.println("No Publication Date Available");
        }

        /*
         * source column
         */

        //get source child index

        boolean hasTag = false;
        index = getChildElement(item, "source");
        out.print("<td>");
        //if source has a url then output with appropriate tags
        if ((index != -1) && (item.child(index).hasAttribute("url"))) {
            out.print("<a href= " + item.child(index).attributeValue("url")
                    + "> ");
            hasTag = true;
        }
        //if there is a source and it has a text child
        if ((index != -1) && (item.child(index).numberOfChildren() > 0)) {
            out.print(item.child(index).child(0).label());

        } else {
            out.println("No Source Available");

        }
        //if used the link, output end tag
        if (hasTag) {
            out.println("</a>");
        }
        //end tag
        out.println("</td>");

        /*
         * title column
         */

        //get link child index
        hasTag = false;
        int index2 = getChildElement(item, "link");
        //opening tag for table
        out.print("<td>");

        //if there is a link, output link with appropriate tags
        if (index2 != -1) {
            out.print("<a href= " + item.child(index2).child(0).label() + "> ");
            hasTag = true;
        }

        //gets title child
        index = getChildElement(item, "title");

        //print title name
        if ((index != -1) && (item.child(index).numberOfChildren() > 0)) {
            out.print(item.child(index).child(0).label());

        }
        //if no title child then use description
        else if ((index == -1)
                && (getChildElement(item, "description") != -1)) {
            index = getChildElement(item, "description");
        } else {
            out.println("No Title Available");
        }

        //if link was used, output end tag
        if (hasTag) {
            out.println("</a>");
        }

        out.println("</td>");

    }

    /**
     * Processes one XML RSS (version 2.0) feed from a given URL converting it
     * into the corresponding HTML output file.
     *
     * @param url
     *            the URL of the RSS feed
     * @param file
     *            the name of the HTML output file
     * @param out
     *            the output stream to report progress or errors
     * @updates out.content
     * @requires out.is_open
     * @ensures <pre>
     * [reads RSS feed from url, saves HTML document with table of news items
     *   to file, appends to out.content any needed messages]
     * </pre>
     */
    private static void processFeed(String url, String file, SimpleWriter out) {

        /*
         * Read XML input and initialize XMLTree. If input is not legal XML,
         * this statement will fail.
         */
        XMLTree xml = new XMLTree1(url);
        XMLTree channel = xml.child(0);

        //<html> <head> <title>the channel tag title as the page title</title>
        int index = getChildElement(channel, "title");
        out.println("<html> <head> <title>" + channel.child(index).label()
                + "</title> ");

        // </head> <body>
        out.println("</head> <body>");

        /*
         * output title, link, and description
         */

        //<h1>the page title inside a link to the <channel> link</h1>
        boolean hasLink = false;
        out.print("<h1>");
        index = getChildElement(channel, "link");
        //if the link isn't blank
        if (channel.child(index).child(0).label().compareTo("") != 0) {
            //output the link as an attribute
            out.print(
                    "<a href= " + channel.child(index).child(0).label() + "> ");
            hasLink = true;
        }

        index = getChildElement(channel, "title");
        //if title isn't blank
        if ((channel.child(index).numberOfChildren() > 0)
                && (channel.child(index).child(0).label().compareTo("") != 0)) {
            //output the title
            out.print(channel.child(index).child(0).label());
        } else {
            //otherwise output empty
            out.print("Empty Title");

        }
        //if the link was used, close up the link tag
        if (hasLink) {
            out.print("</a>");
        }
        out.println("</h1>");

        //<p>
        out.println("<p>");
        //the channel description
        index = getChildElement(channel, "description");
        if ((channel.child(index).numberOfChildren() > 0)
                && (channel.child(index).child(0).label().compareTo("") != 0)) {
            out.println(channel.child(index).child(0).label());

        } else {
            //output empty otherwise
            out.println("Empty Description");

        }

        out.println("</p>");
        out.println("<table border=\'1\'>");
        out.println("<tr>");
        out.print("<th>Date</th>");
        out.print("<th>Source</th>");
        out.print("<th>News</th>");
        out.print("</tr>");

        /*
         * for each item, output title (or description, if title is not
         * available) and link (if available)
         */

        int count = getChildElement(channel, "item");

        while ((count != -1) && (count < channel.numberOfChildren())) {
            out.println("<tr>");
            if (channel.child(count).label().compareTo("item") == 0) {
                processItem(channel.child(count), out);
            }
            out.println("</tr>");
            count++;

        }

        //output the footer
        out.println("</table>");
        out.println("</body> </html>");

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        /*
         * Input the source URL.
         */
        out.println(
                "Enter the file name of a list of urls for RSS 2.0 news feeds: ");
        String url = in.nextLine();

        /*
         * Input: output file name
         */
        out.println("Enter the name of the output file");
        String file = in.nextLine();

        SimpleWriter outH = new SimpleWriter1L(file);

        XMLTree xml = new XMLTree1(url);
        boolean isRSS = false;
        int index = 0;

        //<html> <head> <title>the channel tag title as the page title</title>
        outH.println("<html> <head> <title>");
        outH.println(file);
        outH.println("</title> ");

        if (xml.hasAttribute("title")) {
            outH.println("<h1>" + xml.attributeValue("title") + "</h1>");
        }

        //makes an unordered list
        outH.println("<ul>");

        /*
         * traverse through all the rss feeds in the list
         */
        while (index < xml.numberOfChildren()) {

            isRSS = true;

            if (isRSS) {
                outH.println("<li> <a href= "
                        + xml.child(index).attributeValue("file") + ">"
                        + xml.child(index).attributeValue("name") + "</li>");

                SimpleWriter outFile = new SimpleWriter1L(
                        xml.child(index).attributeValue("file"));

                if (xml.child(index).hasAttribute("file")) {

                    processFeed(xml.child(index).attributeValue("url"),
                            xml.child(index).attributeValue("file"), outFile);

                    outFile.close();
                }
            }

            index++;
        }
        outH.println("</ul>");
        outH.println("</table>");
        outH.println("</body> </html>");

        in.close();
        out.close();
        outH.close();

    }

}