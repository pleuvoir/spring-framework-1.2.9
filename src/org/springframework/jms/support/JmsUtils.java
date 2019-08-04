/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jms.support;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jms.InvalidClientIDException;
import org.springframework.jms.InvalidDestinationException;
import org.springframework.jms.InvalidSelectorException;
import org.springframework.jms.JmsException;
import org.springframework.jms.JmsSecurityException;
import org.springframework.jms.MessageEOFException;
import org.springframework.jms.MessageFormatException;
import org.springframework.jms.MessageNotReadableException;
import org.springframework.jms.MessageNotWriteableException;
import org.springframework.jms.ResourceAllocationException;
import org.springframework.jms.TransactionInProgressException;
import org.springframework.jms.TransactionRolledBackException;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.util.Assert;

/**
 * Generic utility methods for working with JMS. Mainly for internal use
 * within the framework, but also useful for custom JMS access code.
 *
 * @author Juergen Hoeller
 * @since 1.1
 */
public abstract class JmsUtils {

	private static final Log logger = LogFactory.getLog(JmsUtils.class);


	/**
	 * Close the given JMS Connection and ignore any thrown exception.
	 * This is useful for typical <code>finally</code> blocks in manual JMS code.
	 * @param con the JMS Connection to close (may be <code>null</code>)
	 */
	public static void closeConnection(Connection con) {
		if (con != null) {
			try {
				con.close();
			}
			catch (JMSException ex) {
				logger.debug("Could not close JMS Connection", ex);
			}
			catch (Throwable ex) {
				// We don't trust the JMS provider: It might throw RuntimeException or Error.
				logger.debug("Unexpected exception on closing JMS Connection", ex);
			}
		}
	}

	/**
	 * Close the given JMS Session and ignore any thrown exception.
	 * This is useful for typical <code>finally</code> blocks in manual JMS code.
	 * @param session the JMS Session to close (may be <code>null</code>)
	 */
	public static void closeSession(Session session) {
		if (session != null) {
			try {
				session.close();
			}
			catch (JMSException ex) {
				logger.debug("Could not close JMS Session", ex);
			}
			catch (Throwable ex) {
				// We don't trust the JMS provider: It might throw RuntimeException or Error.
				logger.debug("Unexpected exception on closing JMS Session", ex);
			}
		}
	}

	/**
	 * Close the given JMS MessageProducer and ignore any thrown exception.
	 * This is useful for typical <code>finally</code> blocks in manual JMS code.
	 * @param producer the JMS MessageProducer to close (may be <code>null</code>)
	 */
	public static void closeMessageProducer(MessageProducer producer) {
		if (producer != null) {
			try {
				producer.close();
			}
			catch (JMSException ex) {
				logger.debug("Could not close JMS MessageProducer", ex);
			}
			catch (Throwable ex) {
				// We don't trust the JMS provider: It might throw RuntimeException or Error.
				logger.debug("Unexpected exception on closing JMS MessageProducer", ex);
			}
		}
	}

	/**
	 * Close the given JMS MessageConsumer and ignore any thrown exception.
	 * This is useful for typical <code>finally</code> blocks in manual JMS code.
	 * @param consumer the JMS MessageConsumer to close (may be <code>null</code>)
	 */
	public static void closeMessageConsumer(MessageConsumer consumer) {
		if (consumer != null) {
			try {
				consumer.close();
			}
			catch (JMSException ex) {
				logger.debug("Could not close JMS MessageConsumer", ex);
			}
			catch (Throwable ex) {
				// We don't trust the JMS provider: It might throw RuntimeException or Error.
				logger.debug("Unexpected exception on closing JMS MessageConsumer", ex);
			}
		}
	}

	/**
	 * Commit the Session if not within a JTA transaction.
	 * @param session the JMS Session to commit
	 * @throws JMSException if committing failed
	 */
	public static void commitIfNecessary(Session session) throws JMSException {
		Assert.notNull(session, "Session must not be null");
		try {
			session.commit();
		}
		catch (javax.jms.TransactionInProgressException ex) {
			// Ignore -> can only happen in case of a JTA transaction.
		}
		catch (javax.jms.IllegalStateException ex) {
			// Ignore -> can only happen in case of a JTA transaction.
		}
	}

	/**
	 * Rollback the Session if not within a JTA transaction.
	 * @param session the JMS Session to rollback
	 * @throws JMSException if committing failed
	 */
	public static void rollbackIfNecessary(Session session) throws JMSException {
		Assert.notNull(session, "Session must not be null");
		try {
			session.rollback();
		}
		catch (javax.jms.TransactionInProgressException ex) {
			// Ignore -> can only happen in case of a JTA transaction.
		}
		catch (javax.jms.IllegalStateException ex) {
			// Ignore -> can only happen in case of a JTA transaction.
		}
	}

	/**
	 * Convert the specified checked {@link javax.jms.JMSException JMSException} to
	 * a Spring runtime {@link org.springframework.jms.JmsException JmsException}
	 * equivalent.
	 * @param ex the original checked JMSException to convert
	 * @return the Spring runtime JmsException wrapping the given exception
	 */
	public static JmsException convertJmsAccessException(JMSException ex) {
		Assert.notNull(ex, "JMSException must not be null");

		if (ex instanceof javax.jms.IllegalStateException) {
			return new org.springframework.jms.IllegalStateException((javax.jms.IllegalStateException) ex);
		}
		if (ex instanceof javax.jms.InvalidClientIDException) {
			return new InvalidClientIDException((javax.jms.InvalidClientIDException) ex);
		}
		if (ex instanceof javax.jms.InvalidDestinationException) {
			return new InvalidDestinationException((javax.jms.InvalidDestinationException) ex);
		}
		if (ex instanceof javax.jms.InvalidSelectorException) {
			return new InvalidSelectorException((javax.jms.InvalidSelectorException) ex);
		}
		if (ex instanceof javax.jms.JMSSecurityException) {
			return new JmsSecurityException((javax.jms.JMSSecurityException) ex);
		}
		if (ex instanceof javax.jms.MessageEOFException) {
			return new MessageEOFException((javax.jms.MessageEOFException) ex);
		}
		if (ex instanceof javax.jms.MessageFormatException) {
			return new MessageFormatException((javax.jms.MessageFormatException) ex);
		}
		if (ex instanceof javax.jms.MessageNotReadableException) {
			return new MessageNotReadableException((javax.jms.MessageNotReadableException) ex);
		}
		if (ex instanceof javax.jms.MessageNotWriteableException) {
			return new MessageNotWriteableException((javax.jms.MessageNotWriteableException) ex);
		}
		if (ex instanceof javax.jms.ResourceAllocationException) {
			return new ResourceAllocationException((javax.jms.ResourceAllocationException) ex);
		}
		if (ex instanceof javax.jms.TransactionInProgressException) {
			return new TransactionInProgressException((javax.jms.TransactionInProgressException) ex);
		}
		if (ex instanceof javax.jms.TransactionRolledBackException) {
			return new TransactionRolledBackException((javax.jms.TransactionRolledBackException) ex);
		}

		// fallback
		return new UncategorizedJmsException(ex);
	}

}
