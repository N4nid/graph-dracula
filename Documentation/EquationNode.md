## **Variables**: 

- **state**: Can have 4 possible states: 0: is a Number, 1: is a [[Variable]], 2: is an [[Operator]] or 3: is a [[SpecialFunction]]
- **value**: Can be either a double or a string
- [[EquationNode]] left and right (it's a binary tree)

## Methods

- calculate(), recursively calculates it's sub-tree. Has the current x and y position as an input (must always be provided), and an array of type [[Variable]], if you have custom parameters (like a). If the function doesn't have parameters you can just parse a null-pointer.