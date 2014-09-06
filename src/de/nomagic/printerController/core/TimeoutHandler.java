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
package de.nomagic.printerController.core;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public interface TimeoutHandler
{
    int ERROR_FAILED_TO_CREATE_TIMEOUT = -1;

    /** creates a new Timeout.
     *
     * That timeout is identified by the returned ID.
     *
     * @param e Event that contains the src (the function that will be called once the timeout occurs,
     *          and the Parameter that will be passed to that function.
     * @param ms time until the timeout should occur in milliseconds.
     * @return the id of the newly created timeout or ERROR_FAILED_TO_CREATE_TIMEOUT
     *         if no more timeouts can be registered.
     */
    int createTimeout(Event e, int ms);

    /** start the clock on the timeout.
     *
     * can also be used to reset the timeout. This way the timeout can be used as a watchdog.
     * @param timeoutId the ID of the timeout to start
     */
    void startTimeout(int timeoutId);

    /** stop the clock on the timeout.
     *
     * this timeout is not needed anymore.
     *
     * @param timeoutId the ID of the timeout to stop
     */
    void stopTimeout(int timeoutId);
}
