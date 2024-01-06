import os
import shutil




def buildEnvironment(env_variables,source_directory, destination_directory):

    # Create the destination directory if it doesn't exist
    if not os.path.exists(destination_directory):
        os.makedirs(destination_directory)

    # Iterate over each text file in the source directory
    for filename in os.listdir(source_directory):
        if filename.endswith('.yml'):
            source_path = os.path.join(source_directory, filename)
            destination_path = os.path.join(destination_directory, filename)

            # Read the contents of the source text file
            with open(source_path, 'r') as file:
                file_contents = file.read()

            # Replace environment variable placeholders with their corresponding values
            for key, value in env_variables.items():
                placeholder = f'${{{key}}}'
                file_contents = file_contents.replace(placeholder, value)

            # Write the modified contents to the destination file
            with open(destination_path, 'w') as file:
                file.write(file_contents)

            print(f"Modified file: {destination_path}")

    #store the env used in an environment file
    with open(os.path.join(destination_directory,".env"), 'w') as fenv:
        for key, value in env_variables.items() :
            if(value):
                fenv.write(f'{key}={value}\n')
            else:
                fenv.write(f'{key}=\"\"\n')

    # Copy non-text files from source to destination directory
    for filename in os.listdir(source_directory):
        source_path = os.path.join(source_directory, filename)
        destination_path = os.path.join(destination_directory, filename)

        if not filename.endswith('.yml') or filename.endswith("services.yml") or filename.endswith("rules.yml"):
            shutil.copy2(source_path, destination_path)