import sys
import json
import io
import base64
import matplotlib.pyplot as plt

def main():
    # Read JSON from stdin
    input_data = sys.stdin.read()
    if not input_data:
        sys.exit(1)

    data = json.loads(input_data)

    categories = list(data.keys())
    amounts = list(data.values())

    # Generate Pie Chart
    plt.figure(figsize=(6, 6))
    plt.pie(amounts, labels=categories, autopct='%1.1f%%', startangle=140)
    plt.title('Expenses by Category')

    # Save image to memory buffer
    buffer = io.BytesIO()
    plt.savefig(buffer, format='png', bbox_inches='tight')
    plt.close()
    buffer.seek(0)

    # Output Base64 string to stdout
    base64_image = base64.b64encode(buffer.getvalue()).decode('utf-8')
    sys.stdout.write(base64_image)

if __name__ == "__main__":
    main()