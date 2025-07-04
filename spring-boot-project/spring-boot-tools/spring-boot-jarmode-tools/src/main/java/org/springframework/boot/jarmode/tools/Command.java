/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.jarmode.tools;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * A command that can be launched.
 *
 * @author Phillip Webb
 * @author Scott Frederick
 * @author Moritz Halbritter
 */
abstract class Command {

	private final String name;

	private final String description;

	private final Options options;

	private final Parameters parameters;

	/**
	 * Create a new {@link Command} instance.
	 * @param name the name of the command
	 * @param description a description of the command
	 * @param options the command options
	 * @param parameters the command parameters
	 */
	Command(String name, String description, Options options, Parameters parameters) {
		this.name = name;
		this.description = description;
		this.options = options;
		this.parameters = parameters;
	}

	/**
	 * Return the name of this command.
	 * @return the command name
	 */
	String getName() {
		return this.name;
	}

	/**
	 * Return the description of this command.
	 * @return the command description
	 */
	String getDescription() {
		return this.description;
	}

	/**
	 * Return options that this command accepts.
	 * @return the command options
	 */
	Options getOptions() {
		return this.options;
	}

	/**
	 * Return parameters that this command accepts.
	 * @return the command parameters
	 */
	Parameters getParameters() {
		return this.parameters;
	}

	/**
	 * Run the command by processing the remaining arguments.
	 * @param out stream for command output
	 * @param args a mutable deque of the remaining arguments
	 */
	final void run(PrintStream out, Deque<String> args) {
		List<String> parameters = new ArrayList<>();
		Map<Option, String> options = new HashMap<>();
		while (!args.isEmpty()) {
			String arg = args.removeFirst();
			Option option = this.options.find(arg);
			if (option != null) {
				options.put(option, option.claimArg(args));
			}
			else {
				parameters.add(arg);
			}
		}
		run(out, options, parameters);
	}

	/**
	 * Run the actual command.
	 * @param out stream for command output
	 * @param options any options extracted from the arguments
	 * @param parameters any parameters extracted from the arguments
	 */
	abstract void run(PrintStream out, Map<Option, String> options, List<String> parameters);

	/**
	 * Whether the command is deprecated.
	 * @return whether the command is deprecated
	 */
	boolean isDeprecated() {
		return false;
	}

	/**
	 * Returns the deprecation message.
	 * @return the deprecation message
	 */
	String getDeprecationMessage() {
		return null;
	}

	/**
	 * Static method that can be used to find a single command from a collection.
	 * @param commands the commands to search
	 * @param name the name of the command to find
	 * @return a {@link Command} instance or {@code null}.
	 */
	static Command find(Collection<? extends Command> commands, String name) {
		for (Command command : commands) {
			if (command.getName().equals(name)) {
				return command;
			}
		}
		return null;
	}

	/**
	 * Parameters that the command accepts.
	 */
	static final class Parameters {

		private final List<String> descriptions;

		private Parameters(String[] descriptions) {
			this.descriptions = Collections.unmodifiableList(Arrays.asList(descriptions));
		}

		/**
		 * Return the parameter descriptions.
		 * @return the descriptions
		 */
		List<String> getDescriptions() {
			return this.descriptions;
		}

		@Override
		public String toString() {
			return this.descriptions.toString();
		}

		/**
		 * Factory method used if there are no expected parameters.
		 * @return a new {@link Parameters} instance
		 */
		static Parameters none() {
			return of();
		}

		/**
		 * Factory method used to create a new {@link Parameters} instance with specific
		 * descriptions.
		 * @param descriptions the parameter descriptions
		 * @return a new {@link Parameters} instance with the given descriptions
		 */
		static Parameters of(String... descriptions) {
			return new Parameters(descriptions);
		}

	}

	/**
	 * Options that the command accepts.
	 */
	static final class Options {

		private final Option[] values;

		private Options(Option[] values) {
			this.values = values;
		}

		private Option find(String arg) {
			if (arg.startsWith("--")) {
				String name = arg.substring(2);
				for (Option candidate : this.values) {
					if (candidate.getName().equals(name)) {
						return candidate;
					}
				}
				throw new UnknownOptionException(name);
			}
			return null;
		}

		/**
		 * Return if this options collection is empty.
		 * @return if there are no options
		 */
		boolean isEmpty() {
			return this.values.length == 0;
		}

		/**
		 * Return a stream of each option.
		 * @return a stream of the options
		 */
		Stream<Option> stream() {
			return Arrays.stream(this.values);
		}

		/**
		 * Factory method used if there are no expected options.
		 * @return a new {@link Options} instance
		 */
		static Options none() {
			return of();
		}

		/**
		 * Factory method used to create a new {@link Options} instance with specific
		 * values.
		 * @param values the option values
		 * @return a new {@link Options} instance with the given values
		 */
		static Options of(Option... values) {
			return new Options(values);
		}

	}

	/**
	 * An individual option that the command can accepts. Can either be an option with a
	 * value (e.g. {@literal --log debug}) or a flag (e.g. {@literal
	 * --verbose}). It also can be both if the value is marked as optional.
	 */
	static final class Option {

		private final String name;

		private final String valueDescription;

		private final String description;

		private final boolean optionalValue;

		private Option(String name, String valueDescription, String description, boolean optionalValue) {
			this.name = name;
			this.description = description;
			this.valueDescription = valueDescription;
			this.optionalValue = optionalValue;
		}

		/**
		 * Return the name of the option.
		 * @return the options name
		 */
		String getName() {
			return this.name;
		}

		/**
		 * Return the description of the expected argument value or {@code null} if this
		 * option is a flag/switch.
		 * @return the option value description
		 */
		String getValueDescription() {
			return this.valueDescription;
		}

		/**
		 * Return the name and the value description combined.
		 * @return the name and value description
		 */
		String getNameAndValueDescription() {
			return this.name + ((this.valueDescription != null) ? " " + this.valueDescription : "");
		}

		/**
		 * Return a description of the option.
		 * @return the option description
		 */
		String getDescription() {
			return this.description;
		}

		private String claimArg(Deque<String> args) {
			if (this.valueDescription == null) {
				return null;
			}
			if (this.optionalValue) {
				String nextArg = args.peek();
				return (nextArg != null && !nextArg.startsWith("--")) ? args.removeFirst() : null;
			}
			try {
				return args.removeFirst();
			}
			catch (NoSuchElementException ex) {
				throw new MissingValueException(this.name);
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			return this.name.equals(((Option) obj).name);
		}

		@Override
		public int hashCode() {
			return this.name.hashCode();
		}

		@Override
		public String toString() {
			return this.name;
		}

		/**
		 * Factory method to create a flag/switch option.
		 * @param name the name of the option
		 * @param description a description of the option
		 * @return a new {@link Option} instance
		 */
		static Option flag(String name, String description) {
			return new Option(name, null, description, false);
		}

		/**
		 * Factory method to create value option.
		 * @param name the name of the option
		 * @param valueDescription a description of the expected value
		 * @param description a description of the option
		 * @return a new {@link Option} instance
		 */
		static Option of(String name, String valueDescription, String description) {
			return new Option(name, valueDescription, description, false);
		}

		/**
		 * Factory method to create value option.
		 * @param name the name of the option
		 * @param valueDescription a description of the expected value
		 * @param description a description of the option
		 * @param optionalValue whether the value is optional
		 * @return a new {@link Option} instance
		 */
		static Option of(String name, String valueDescription, String description, boolean optionalValue) {
			return new Option(name, valueDescription, description, optionalValue);
		}

	}

}
