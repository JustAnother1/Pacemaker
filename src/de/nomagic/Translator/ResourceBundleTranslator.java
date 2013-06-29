/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses/>
 *
 */
package de.nomagic.Translator;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class ResourceBundleTranslator extends Translator
{
    /** File Name of Message Bundle Files. */
    public static final String MESSAGE_BUNDLE_BASE_NAME = "res/MessagesBundle";

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private ResourceBundle messages = null;

    public ResourceBundleTranslator(Locale curLocale)
    {
        if(null == curLocale)
        {
            curLocale = Locale.getDefault();
        }
        String prefix = null;
        try
        {
            prefix = MESSAGE_BUNDLE_BASE_NAME;
            messages = ResourceBundle.getBundle(prefix, curLocale);
        }
        catch(final MissingResourceException e )
        {
            log.error("ERROR : Did not find the ressouce bundle !("
                               + prefix + "_"
                               + curLocale.toString() + ".properties)");
        }
    }

    @Override
    public String t(String key)
    {
        // TODO Auto-generated method stub
        if(null == messages)
        {
            return key;
        }
        return messages.getString(key);
    }

}
