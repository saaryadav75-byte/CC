// 2037/C Superultra's Favorite Permutation

#include <iostream>
using namespace std;
 
int main() {
    int a;
    cin >> a;
    while (a--) {
        int n;
        cin >> n;
        if (n <= 4) {
            cout << -1 << endl;
        } else {
            cout << 1 << " " << 3 << " ";
            for (int i = 7; i <= n; i += 2) {
                cout << i << " ";
            }
            cout << 5 << " " << 4 << " " << 2 << " ";
            for (int i = 6; i <= n; i += 2) {
                cout << i << " ";
            }
            cout << endl;
        }
    }
    return 0;
}
