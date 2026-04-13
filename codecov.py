import sys
import defusedxml.ElementTree as ET

try:
    tree = ET.parse('target/site/jacoco/jacoco.xml')
    root = tree.getroot()
except (ET.ParseError, FileNotFoundError) as e:
    print(f"Error parsing XML file: {e}", file=sys.stderr)
    sys.exit(1)

for package in root.findall('package') or []:
    if package.attrib.get('name', '') == 'org/barcelonajug/superherobattlearena/application/usecase':
        for class_ in package.findall('class') or []:
            if class_.attrib.get('name', '') == 'org/barcelonajug/superherobattlearena/application/usecase/MatchUseCase':
                for method in class_.findall('method') or []:
                    for counter in method.findall('counter') or []:
                        if counter.attrib.get('type', '') == 'INSTRUCTION':
                            method_name = method.attrib.get('name', '')
                            missed = counter.attrib.get('missed', '0')
                            covered = counter.attrib.get('covered', '0')
                            print(f"Method: {method_name}, Instructions: Missed={missed}, Covered={covered}")