import os
import sys

# Add this print statement to see where the script is being executed from
print(f"Current Working Directory: {os.getcwd()}")

def main():
    """Run administrative tasks."""
    # Add this print statement to see what settings module Django is trying to load
    settings_module = os.environ.get('DJANGO_SETTINGS_MODULE')
    print(f"DJANGO_SETTINGS_MODULE: {settings_module}")

    os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cyberSniffer.settings')
    try:
        from django.core.management import execute_from_command_line
    except ImportError as exc:
        raise ImportError(
            "Couldn't import Django. Are you sure it's installed and "
            "available on your PYTHONPATH environment variable? Did you "
            "forget to activate a virtual environment?"
        ) from exc
    execute_from_command_line(sys.argv)

if __name__ == '__main__':
    main()
