// 327/A  Flipping Game

#include <bits/stdc++.h>
using namespace std;

int main(){
    int n;
    cin>>n;
    int arr[n];
    for(int i=0;i<n;i++){
        cin>>arr[i];
    }

    int one=0;
    for(int i=0;i<n;i++){
        if(arr[i]==1){
            one++;
        }
    }

    int ans=0;
    for(int i=0;i<n;i++){
        int zero=0;
        int ois=0;
        for(int j=i;j<n;j++){
            if(arr[j]==0) zero++;
            else ois++;
            int newone=one-ois+zero;
            ans=max(ans,newone);
        }
    }

    cout<<ans;
    return 0;
}
