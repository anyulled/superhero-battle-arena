import xml.etree.ElementTree as ET

tree = ET.parse('target/site/jacoco/jacoco.xml')
root = tree.getroot()

for package in root.findall('package'):
    if package.attrib['name'] == 'org/barcelonajug/superherobattlearena/application/usecase':
        for class_ in package.findall('class'):
            if class_.attrib['name'] == 'org/barcelonajug/superherobattlearena/application/usecase/MatchUseCase':
                for method in class_.findall('method'):
                    for counter in method.findall('counter'):
                        if counter.attrib['type'] == 'INSTRUCTION':
                            print(f"Method: {method.attrib['name']}, Instructions: Missed={counter.attrib['missed']}, Covered={counter.attrib['covered']}")
