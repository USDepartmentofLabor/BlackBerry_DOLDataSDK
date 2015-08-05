/**
 * HTTPDemo.java
 *
 * Copyright � 1998-2012 Research In Motion Ltd.
 *
 * Note: For the sake of simplicity, this sample application may not leverage
 * resource bundles and resource strings.  However, it is STRONGLY recommended
 * that application developers make use of the localization features available
 * within the BlackBerry development platform to ensure a seamless application
 * experience across a variety of languages and geographies.  For more information
 * on localizing your application, please refer to the BlackBerry Java Development
 * Environment Development Guide associated with this release.
 */

package com.rim.samples.device.httpdemo;

import java.io.*;
import javax.microedition.io.*;

import net.rim.device.api.command.*;
import net.rim.device.api.io.*;
import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.util.*;

/**
 * This sample makes a an http or https connection to a specified URL and
 * retrieves and displays html content.
 */
public class HTTPDemo extends UiApplication
{
    private static final String SAMPLE_HTTPS_PAGE = "www.example.com";
    private static final String[] HTTP_PROTOCOL = {"http://", "https://"};
    private static final char HTML_TAG_OPEN = '[';
    private static final char HTML_TAG_CLOSE = ']';
    private static final String HEADER_CONTENTTYPE = "content-type";
    private static final String CONTENTTYPE_TEXTHTML = "text/html";
    private static final String CONTENTTYPE_APPJSON = "application/json";
    private static final String CONTENTYPE_X_APIKEY = "YOUR-QUARRY-API-KEY";
    private static final int STATE_0 = 0;
    private static final int STATE_1 = 1;
    private static final int STATE_2 = 2;
    private static final int STATE_3 = 3;
    private static final int STATE_4 = 4;
    private static final int STATE_5 = 5;

    private static final char CR = 0x000D;
    private static final char LF = 0x000A;
    private static final char TAB = 0x0009;

    private HTTPDemoScreen _mainScreen;
    private EditField _url;
    private RichTextField _content;
    private boolean _useWapStack;
    private WapOptionsScreen _wapOptionsScreen;

    private StatusThread _statusThread = new StatusThread();
    private ConnectionThread _connectionThread = new ConnectionThread();

   /**
    * Entry point for application
    * @param args Command line arguments (not used)
    */
    public static void main(String[] args)
    {
        // Create a new instance of the application and make the currently
        // running thread the application's event dispatch thread.
        HTTPDemo theApp = new HTTPDemo();
        theApp.enterEventDispatcher();
    }


    /**
     * Creates a new HTTPDemo object
     */
    public HTTPDemo()
    {
        _fetchMenuItem = new MenuItem(new StringProvider("Fetch") , 0x230010, 0);
        _fetchMenuItem.setCommand(new Command(new CommandHandler()
        {
            /**
             * @see net.rim.device.api.command.CommandHandler#execute(ReadOnlyCommandMetadata, Object)
             */
            public void execute(ReadOnlyCommandMetadata metadata, Object context)
            {
                // Don't execute on a blank url.
                if ( _url.getText().length() > 0 )
                {
                    if ( !_connectionThread.isStarted() )
                    {
                        fetchPage(_url.getText());
                    }
                    else
                    {
                        createNewFetch(_url.getText());
                    }
                }
            }
        }));

        _clearContent = new MenuItem(new StringProvider("Clear Content") , 0x230020, 3);
        _clearContent.setCommand(new Command(new CommandHandler()
        {
            /**
             * @see net.rim.device.api.command.CommandHandler#execute(ReadOnlyCommandMetadata, Object)
             */
            public void execute(ReadOnlyCommandMetadata metadata, Object context)
            {
                _content.setText("<content>");
            }
        }));

        _fetchHTTPSPage = new MenuItem(new StringProvider("Fetch Sample HTTPS Page") , 0x230030, 2);
        _fetchHTTPSPage.setCommand(new Command(new CommandHandler()
        {
            /**
             * @see net.rim.device.api.command.CommandHandler#execute(ReadOnlyCommandMetadata, Object)
             */
            public void execute(ReadOnlyCommandMetadata metadata, Object context)
            {
                if ( !_connectionThread.isStarted())
                {
                    // Menu items are executed on the event thread, therefore we can edit the
                    // URL field in place.
                    _url.setText(SAMPLE_HTTPS_PAGE);
                    fetchPage(SAMPLE_HTTPS_PAGE);
                }
                else
                {
                    createNewFetch(_url.getText());
                }
            }
        }));

        _wapStackOption = new MenuItem(new StringProvider("Use Wap Stack") , 0x230040, 4);
        _wapStackOption.setCommand(new Command(new CommandHandler()
        {
            /**
             * @see net.rim.device.api.command.CommandHandler#execute(ReadOnlyCommandMetadata, Object)
             */
            public void execute(ReadOnlyCommandMetadata metadata, Object context)
            {
                _useWapStack = !_useWapStack; // Toggle the wap stack option
            }
        }));

        _wapStackOptionScreen = new MenuItem(new StringProvider("Wap Options"), 0x230050, 5);
        _wapStackOptionScreen.setCommand(new Command(new CommandHandler()
        {
            /**
             * @see net.rim.device.api.command.CommandHandler#execute(ReadOnlyCommandMetadata, Object)
             */
            public void execute(ReadOnlyCommandMetadata metadata, Object context)
            {
                _wapOptionsScreen.display();
            }
        }));

        _wapOptionsScreen = new WapOptionsScreen(this);
        _mainScreen = new HTTPDemoScreen();
        _mainScreen.setTitle("DOL Quarry Demo");

        _url = new EditField("URL: " , "https://", Integer.MAX_VALUE, EditField.FILTER_URL);
        _url.setCursorPosition(7);
        _mainScreen.add(_url);

        _mainScreen.add(new SeparatorField());

        _content = new RichTextField();
        _mainScreen.add(_content);

        // Start the helper threads
        _statusThread.start();
        _connectionThread.start();

        pushScreen(_mainScreen);
    }


    /**
     * Menu item to fetch content from URL specified in URL field
     */
    private MenuItem _fetchMenuItem;

    /**
     * Clears the content field
     */
    private MenuItem _clearContent;

    /**
     * Menu item to fetch pre-defined sample HTTPS page
     */
    private MenuItem _fetchHTTPSPage;

    /**
     * Toggles the wap stack option
     */
    private MenuItem _wapStackOption;

    /**
     * Menu item to display the wap options screen
     */
    private MenuItem _wapStackOptionScreen;

    /**
     * Stops current fetch and initiates a new fetch
     * @param url The url of the content to fetch
     */
    private void createNewFetch(String url)
    {
        // Stop the current helper threads
        _statusThread.stop();
        _connectionThread.stop();

        // Reinitialize the helper threads
        _statusThread = new StatusThread();
        _connectionThread = new ConnectionThread();

        // Restart the helper threads
        _statusThread.start();
        _connectionThread.start();

        // Fetch the url
        fetchPage(url);
    }


    /**
     * Fetches the content on the specified url
     * @param url The url of the content to fetch
     */
    private void fetchPage(String url)
    {
        // Normalize the url
        String lcase = url.toLowerCase();

        boolean validHeader = false;
        int i = 0;

        for (i = HTTP_PROTOCOL.length - 1; i >= 0; --i)
        {
            if ( -1 != lcase.indexOf(HTTP_PROTOCOL[i]) )
            {
                validHeader = true;
                break;
            }
        }

        if ( _useWapStack )
        {
            url = url + _wapOptionsScreen.getWapParameters();
        }

        if ( !validHeader )
        {
            url = HTTP_PROTOCOL[0] + url; // Prepend the protocol specifier
        }

         // It is illegal to open a connection on the event thread. We need to
         // spawn a new thread for connection operations.
        _connectionThread.fetch(url);

        // Create a thread to display the status of the current operation
        _statusThread.go();
    }


    /**
     * Method to update the content field
     * @param text The text to display
     */
    private void updateContent(final String text)
    {
        // This will create significant garbage, but avoids threading issues
        // (compared with creating a static Runnable and setting the text).
        UiApplication.getUiApplication().invokeLater(new Runnable()
        {
            public void run()
            {
                _content.setText(text);
            }
        });
    }

    /**
     * Performs operations on the html data. Removes tags, comments, whitespace
     * and inserts new lines for the <p> tag.
     * @param text The text to be prepared for display
     * @return The processed text
     */
    private String prepareData(String text)
    {
        final int text_length = text.length();
        StringBuffer data = new StringBuffer(text_length);
        int state = STATE_0;
        int count = 0;
        int writeIndex = -1;
        char c = (char)0;

        for ( int i = 0; i < text_length; ++i)
        {
            c = text.charAt(i);
            switch ( state )
            {
                case STATE_0:
                    if ( c == HTML_TAG_OPEN )
                    {
                        ++count;
                        state = STATE_1;
                    }
                    else if ( c == ' ' )
                    {
                        data.insert(++writeIndex, c);
                        state = STATE_5;
                    }
                    else if ( !specialChar(c) )
                    {
                        data.insert(++writeIndex, c);
                    }
                    break;

                case STATE_1:
                    if ( c == '!' && text.charAt(i + 1) == '-' && text.charAt(i + 2) == '-' )
                    {
                        System.out.println("Entering Comment state");
                        i += 2;
                        state = STATE_3;
                    }
                    else if ( Character.toLowerCase(c) == 'p' )
                    {
                        state = STATE_4;
                    }
                    else if ( c == HTML_TAG_CLOSE )
                    {
                        --count;
                        state = STATE_0;
                    }
                    else
                    {
                        state = STATE_2;
                    }
                    break;

                case STATE_2:
                    if ( c == HTML_TAG_OPEN )
                    {
                        ++count;
                    }
                    else if ( c == HTML_TAG_CLOSE )
                    {
                        if( --count == 0 )
                        {
                            state = STATE_0;
                        }
                    }
                    break;

                case STATE_3:
                    if ( c == '-' && text.charAt(i+1) == '-' && text.charAt(i + 2) == HTML_TAG_CLOSE )
                    {
                        --count;
                        i += 2;
                        state = STATE_0;
                        System.out.println("Exiting comment state");
                    }
                    break;

                case STATE_4:
                    if ( c == HTML_TAG_CLOSE )
                    {
                        --count;
                        data.insert(++writeIndex, '\n');
                        state = STATE_0;
                    }
                    else
                    {
                        state = STATE_1;
                    }
                    break;

                case STATE_5:
                    if ( c == HTML_TAG_OPEN )
                    {
                        ++count;
                        state = STATE_1;
                    }
                    else if ( c != ' ' )
                    {
                        state = STATE_0;
                        if ( !specialChar(c) )
                        {
                            data.insert(++writeIndex, c);
                        }
                    }
                    break;
            }
        }

        return data.toString().substring(0, writeIndex + 1);
    }


    /**
     * Checks whether a char is a carriage return, line feed or tab character
     * @param c The char to check
     * @return True if char is a carriage return or a line feed, otherwise false
     */
    private boolean specialChar(char c)
    {
        return c == LF || c == CR || c == TAB;
    }


    /**
     * The ConnectionThread class manages the HTTP connection. If a fetch call
     * is made and another request is made while the first is still active, the
     * first fetch will be terminated and the second one will start processing.
     */
    private class ConnectionThread extends Thread
    {
        private static final int TIMEOUT = 100; // ms

        private String _theUrl;

        private volatile boolean _fetchStarted = false;
        private volatile boolean _stop = false;

        /**
         * Retrieves the url this thread is trying to connect to
         * @return The url that this thread is trying to connect to
         */
        private String getUrl()
        {
            return _theUrl;
        }


        /**
         * Indicates whether the thread has started fetching yet
         * @return True if the fetching has started, false otherwise
         */
        private boolean isStarted()
        {
            return _fetchStarted;
        }


        /**
         * Fetches a page
         * @param url The url of the page to fetch
         */
        private void fetch(String url)
        {
            _fetchStarted = true;
            _theUrl = url;
        }


        /**
         * Stop the thread
         */
        private void stop()
        {
            _stop = true;
        }


        /**
         * This method is where the thread retrieves the content from the page
         * whose url is associated with this thread.
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            for(;;)
            {
                // Thread control
                while( !_fetchStarted && !_stop)
                {
                    // Sleep for a bit so we don't spin
                    try
                    {
                        sleep(TIMEOUT);
                    }
                    catch (InterruptedException e)
                    {

                        errorDialog("Thread#sleep(long) threw " + e.toString());
                    }
                }

                // Exit condition
                if ( _stop )
                {
                    return;
                }

                String content = "";
                System.out.println("**QUARRY**");
                // Open the connection and extract the data
                try
                {
                	HttpConnection httpConn = (HttpConnection)Connector.open(getUrl());
                	httpConn.setRequestProperty("X-API-KEY", CONTENTYPE_X_APIKEY);

                    int status = httpConn.getResponseCode();
                    System.out.println("**QUARRY RESPONSE CODE**");
                    System.out.println(httpConn.toString());

                    if (status == HttpConnection.HTTP_OK)
                    {
                    	System.out.println("**Quarry BLOCK 1**");

                        // Is this html?
                        String contentType = httpConn.getHeaderField(HEADER_CONTENTTYPE);
                        boolean htmlContent = (contentType != null && contentType.startsWith(CONTENTTYPE_TEXTHTML));

                        InputStream input = httpConn.openInputStream();

                        byte[] bytes = IOUtilities.streamToBytes(input);

                        StringBuffer raw = new StringBuffer(new String(bytes));
                        raw.insert(0, "bytes received]\n");
                        raw.insert(0, bytes.length);
                        raw.insert(0, '[');

                        content = raw.toString();

                        if ( htmlContent )
                        {
                            content = prepareData(raw.toString());
                        }
                        input.close();
                    }
                    else
                    {
                    	System.out.println("**Quarry BLOCK 2**");
                    	// Print full content
                        //content = "response code = " + status;

                        String contentType = httpConn.getHeaderField(HEADER_CONTENTTYPE);
                        boolean jsonContent = (contentType != null && contentType.startsWith(CONTENTTYPE_APPJSON));

                        System.out.println(contentType);
                        InputStream input = httpConn.openInputStream();

                        byte[] bytes = IOUtilities.streamToBytes(input);

                        StringBuffer raw = new StringBuffer(new String(bytes));
                        raw.insert(0, " bytes received] ");
                        raw.insert(0, bytes.length);
                        raw.insert(0, '[');

                        content = raw.toString();
                        input.close();

                    }
                    httpConn.close();
                }
                catch (IOCancelledException e)
                {
                	System.out.println("**Quarry Exception 1**");
                    System.out.println(e.toString());
                    return;
                }
                catch (IOException e)
                {

                	System.out.println("**Quarry Exception 2**");
                	errorDialog(e.toString());
                    return;
                }

                // Make sure status thread doesn't overwrite the content
                stopStatusThread();
                updateContent(content);

                // We're finished with the operation so reset
                // the start state.
                _fetchStarted = false;
            }
        }


        /**
         * Stops the status thread
         */
        private void stopStatusThread()
        {
            _statusThread.pause();
            try
            {
                synchronized(_statusThread)
                {
                    // Check the paused condition, in case the notify
                    // fires prior to our wait, in which  case we may
                    // never see that notify.
                    while ( !_statusThread.isPaused() );
                    {
                        _statusThread.wait();
                    }
                }
            }
            catch (InterruptedException e)
            {
                errorDialog("StatusThread#wait() threw " + e.toString());
            }
        }
    }


    /**
     * The StatusThread class manages display of the status message while lengthy
     * HTTP/HTML operations are taking place.
     */
    private class StatusThread extends Thread
    {
        private static final int TIMEOUT = 500; // ms
        private static final int THREAD_TIMEOUT = 500;

        private volatile boolean _stop = false;
        private volatile boolean _running = false;
        private volatile boolean _isPaused = false;

        /**
         * Resumes this thread
         * @see #pause()
         */
        private void go()
        {
            _running = true;
            _isPaused = false;
        }


        /**
         * Pauses this thread
         * @see #go()
         */
        private void pause()
        {
            _running = false;
        }


        /**
         * Queries the paused status of this thread
         * @return True if the thread is paused, false otherwise
         */
        private boolean isPaused()
        {
            return _isPaused;
        }


        /**
         * Stops the thread
         */
        private void stop()
        {
            _stop = true;
        }


        /**
         * This method is where the thread updates the status message while
         * HTTP/HTML operations are taking place.
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            int i = 0;

            // Set up the status messages
            String[] statusMsg = new String[6];
            StringBuffer status = new StringBuffer("Working");
            statusMsg[0] = status.toString();


            for ( int j = 1; j < 6; ++j)
            {
                statusMsg[j] = status.append(" .").toString();
            }

            for (;;)
            {
                while (!_stop && !_running)
                {
                    // Sleep a bit so we don't spin
                    try
                    {
                        sleep(THREAD_TIMEOUT);
                    }
                    catch ( InterruptedException e)
                    {
                        errorDialog("Thread#sleep(long) threw " + e.toString());
                    }
                }

                if ( _stop )
                {
                    return;
                }

                i = 0;

                // Clear the status buffer
                status.delete(0, status.length());

                for ( ;; )
                {
                    // We're not synchronizing on the boolean flag,
                    // therefore, value is declared volatile.
                    if ( _stop )
                    {
                        return;
                    }

                    if ( !_running )
                    {
                        _isPaused = true;

                        synchronized(this)
                        {
                            this.notify();
                        }

                        break;
                    }

                    updateContent(statusMsg[++i%6]);

                    try
                    {
                        Thread.sleep(TIMEOUT); // Wait for a bit.
                    }
                    catch (InterruptedException e)
                    {
                        errorDialog("Thread.sleep(long) threw " + e.toString());
                    }
                }
            }
        }
    }


    /**
     * This is the main screen that displays the content fetched
     * by the ConnectionThread.
     */
    private class HTTPDemoScreen extends MainScreen
    {
        /**
         * @see net.rim.device.api.ui.container.MainScreen#makeMenu(Menu,int)
         */
        protected void makeMenu(Menu menu, int instance)
        {
            menu.add(_fetchMenuItem);
            menu.add(_clearContent);
            menu.add(_fetchHTTPSPage);
            menu.add(_wapStackOptionScreen);

            StringBuffer sb = new StringBuffer();

            if ( _useWapStack )
            {
                sb.append(Characters.CHECK_MARK);
            }

            sb.append("Use Wap Stack");
            _wapStackOption.setText(new StringProvider(sb.toString()));
            menu.add(_wapStackOption);

            menu.addSeparator();

            super.makeMenu(menu, instance);
        }


        /**
         * @see net.rim.device.api.ui.container.MainScreen#onSavePrompt()
         */
        public boolean onSavePrompt()
        {
            // Prevent the save dialog from being displayed
            return true;
        }


        /**
         * @see net.rim.device.api.ui.Screen#close()
         */
        public void close()
        {
            _statusThread.stop();
            _connectionThread.stop();

            super.close();
        }


        /**
         * @see net.rim.device.api.ui.Screen#keyChar(char,int,int)
         */
        protected boolean keyChar(char key, int status, int time)
        {
            if ( getLeafFieldWithFocus() == _url && key == Characters.ENTER )
            {
                _fetchMenuItem.run();
                return true; // Consume the key event
            }
            else
            {
                return super.keyChar(key, status, time);
            }
        }
    }


    /**
     * Presents a dialog to the user with a given message
     * @param message The text to display
     */
    public static void errorDialog(final String message)
    {
        UiApplication.getUiApplication().invokeLater(new Runnable()
        {
            public void run()
            {
                Dialog.alert(message);
            }
        });
    }
}
